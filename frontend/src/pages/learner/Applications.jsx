import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { FileText, Calendar, DollarSign, Building2, Trash2 } from 'lucide-react';
import { applicationsAPI } from '@/api/applications';
import { bursariesAPI } from '@/api/bursaries';
import { formatCurrency, formatDate, formatRelativeTime } from '@/utils/helpers';
import { APPLICATION_STATUS, STATUS_COLORS } from '@/utils/constants';

function Applications() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [deletingId, setDeletingId] = useState(null);

  const [selectedBursary, setSelectedBursary] = useState(null);
  const [isBursaryModalOpen, setIsBursaryModalOpen] = useState(false);
  const [loadingBursary, setLoadingBursary] = useState(false);

  // Fetch all applications
  const { data: applicationsData, isLoading, error } = useQuery({
    queryKey: ['myApplications'],
    queryFn: async () => {
      try {
        return await applicationsAPI.getMyApplications();
      } catch (error) {
        console.error('Error fetching applications:', error);
        return { data: [] };
      }
    },
    refetchInterval: 2000
  });

  const applications = applicationsData?.data || [];

  // Group applications by status
  const groupedApplications = {
    all: applications,
    submitted: applications.filter(app => app?.status === APPLICATION_STATUS.SUBMITTED),
    under_review: applications.filter(app => app?.status === APPLICATION_STATUS.UNDER_REVIEW),
    shortlisted: applications.filter(app => app?.status === APPLICATION_STATUS.SHORTLISTED),
    accepted: applications.filter(app => app?.status === APPLICATION_STATUS.ACCEPTED),
    rejected: applications.filter(app => app?.status === APPLICATION_STATUS.REJECTED),
  };

  // Withdraw mutation
  const withdrawMutation = useMutation({
    mutationFn: (applicationId) => applicationsAPI.withdraw(applicationId),
    onSuccess: () => {
      queryClient.invalidateQueries(['myApplications']);
      toast({
        title: "Application Withdrawn",
        description: "Your application has been withdrawn successfully.",
      });
      setDeletingId(null);
    },
    onError: (error) => {
      toast({
        title: "Withdrawal Failed",
        description: error?.message || "Failed to withdraw application. Please try again.",
        variant: "destructive",
      });
      setDeletingId(null);
    },
  });

  const handleWithdraw = (applicationId) => {
    if (window.confirm('Are you sure you want to withdraw this application? This action cannot be undone.')) {
      setDeletingId(applicationId);
      withdrawMutation.mutate(applicationId);
    }
  };

  const canWithdraw = (status) => {
    return status === APPLICATION_STATUS.SUBMITTED || status === APPLICATION_STATUS.UNDER_REVIEW;
  };

  const getStatusBadgeClass = (status) => {
    return STATUS_COLORS[status] || 'bg-gray-100 text-gray-800';
  };

const handleViewBursary = async (bursaryId) => {
  try {
    setLoadingBursary(true);

    const response = await bursariesAPI.getById(bursaryId);
    console.log(response?.data);

    setSelectedBursary(response?.data); // or response depending on backend wrapper
    setIsBursaryModalOpen(true);

  } catch (error) {
    toast({
      title: 'Failed to load bursary',
      description: error?.message || 'Something went wrong',
      variant: 'destructive',
    });
  } finally {
    setLoadingBursary(false);
  }
};


  // Application Card Component
  const ApplicationCard = ({ application }) => {
    const bursary = application?.bursary;
    const provider = bursary?.provider;

    return (
      <Card className="hover:shadow-md transition-shadow">
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <CardTitle className="text-lg">
                {bursary?.title || 'Untitled Bursary'}
              </CardTitle>
              <CardDescription className="mt-1">
                {provider?.organizationName || 'Unknown Provider'}
              </CardDescription>
            </div>
            <Badge className={getStatusBadgeClass(application?.status)}>
              {application?.status?.replace(/_/g, ' ').toUpperCase() || 'UNKNOWN'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Key Info */}
          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center gap-2 text-sm">
              <DollarSign className="h-4 w-4 text-muted-foreground" />
              <span className="font-semibold text-green-600">
                {bursary?.amount ? formatCurrency(bursary.amount) : 'N/A'}
              </span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Building2 className="h-4 w-4 text-muted-foreground" />
              <span className="text-muted-foreground">
                {provider?.organizationType || 'N/A'}
              </span>
            </div>
          </div>

          {/* Timeline */}
          <div className="space-y-2 text-sm">
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <span className="text-muted-foreground">Applied:</span>
              <span className="font-medium">
                {application?.appliedAt ? formatRelativeTime(application.appliedAt) : 'N/A'}
              </span>
            </div>
            {application?.lastUpdated && (
              <div className="flex items-center gap-2">
                <FileText className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">Last Updated:</span>
                <span className="font-medium">
                  {formatRelativeTime(application.lastUpdated)}
                </span>
              </div>
            )}
          </div>

          {/* Provider Notes (if any) */}
          {application?.notes && (
            <div className="bg-muted p-3 rounded-lg">
              <p className="text-sm font-medium mb-1">Provider Notes:</p>
              <p className="text-sm text-muted-foreground">{application.notes}</p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-2 pt-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => handleViewBursary(bursary?.id)}
              disabled={loadingBursary}
            >
              View Bursary
            </Button>
            {isBursaryModalOpen && selectedBursary && (
              <Dialog open={isBursaryModalOpen} onOpenChange={setIsBursaryModalOpen}>
                <DialogContent className="max-w-2xl">
                  <DialogHeader>
                    <DialogTitle>{selectedBursary.title}</DialogTitle>
                    <DialogDescription>
                      {selectedBursary.provider?.organizationName}
                    </DialogDescription>
                  </DialogHeader>

                  <div className="space-y-4">
                    <p><strong>Amount:</strong> {formatCurrency(selectedBursary.amount)}</p>
                    <p><strong>Description:</strong> {selectedBursary.description}</p>
                    <p><strong>Application deadline:</strong> {selectedBursary.applicationDeadline}</p>

                    {/* learner-specific info if included */}
                    {selectedBursary.learner && (
                      <div>
                        <h4 className="font-semibold">Your Details</h4>
                        <p>{selectedBursary.learner.fullName}</p>
                      </div>
                    )}
                  </div>
                </DialogContent>
              </Dialog>
            )}
            {canWithdraw(application?.status) && (
              <Button
                variant="destructive"
                size="icon"
                onClick={() => handleWithdraw(application?.id)}
                disabled={deletingId === application?.id}
              >
                <Trash2 className="h-4 w-4" />
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };

  // Loading State
  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">My Applications</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[...Array(4)].map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-20 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">My Applications</h1>
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load applications. Please try refreshing the page.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">My Applications</h1>
        <p className="text-muted-foreground">
          Track the status of your bursary applications
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Total</CardDescription>
            <CardTitle className="text-2xl">{groupedApplications.all?.length || 0}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Submitted</CardDescription>
            <CardTitle className="text-2xl text-blue-600">
              {groupedApplications.submitted?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Under Review</CardDescription>
            <CardTitle className="text-2xl text-yellow-600">
              {groupedApplications.under_review?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Accepted</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {groupedApplications.accepted?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Rejected</CardDescription>
            <CardTitle className="text-2xl text-red-600">
              {groupedApplications.rejected?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="all" className="w-full">
        <TabsList className="grid w-full grid-cols-6">
          <TabsTrigger value="all">
            All ({groupedApplications.all?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="submitted">
            Submitted ({groupedApplications.submitted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="under_review">
            Under Review ({groupedApplications.under_review?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="shortlisted">
            Shortlisted ({groupedApplications.shortlisted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="accepted">
            Accepted ({groupedApplications.accepted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="rejected">
            Rejected ({groupedApplications.rejected?.length || 0})
          </TabsTrigger>
        </TabsList>

        {['all', 'submitted', 'under_review', 'shortlisted', 'accepted', 'rejected'].map(status => (
          <TabsContent key={status} value={status} className="mt-6">
            {groupedApplications[status]?.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                  <p className="text-muted-foreground mb-2">
                    No applications in this category
                  </p>
                  {status === 'all' && (
                    <Button
                      variant="outline"
                      className="mt-4"
                      onClick={() => window.location.href = '/learner/bursaries'}
                    >
                      Browse Bursaries
                    </Button>
                  )}
                </CardContent>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {groupedApplications[status]?.map((application) => (
                  <ApplicationCard key={application?.id} application={application} />
                ))}
              </div>
            )}
          </TabsContent>
        ))}
      </Tabs>
    </div>
  );
}

export default Applications;