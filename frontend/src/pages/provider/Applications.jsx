import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { useToast } from '@/hooks/use-toast';
import { FileText, User, GraduationCap, DollarSign, Calendar, Eye, Edit } from 'lucide-react';
import { applicationsAPI } from '@/api/applications';
import { formatCurrency, formatDate, formatRelativeTime } from '@/utils/helpers';
import { APPLICATION_STATUS, STATUS_COLORS } from '@/utils/constants';

function ProviderApplications() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const [selectedApplication, setSelectedApplication] = useState(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateForm, setUpdateForm] = useState({
    status: '',
    notes: '',
  });

  // Fetch all applications
  const { data: applicationsData, isLoading, error } = useQuery({
    queryKey: ['providerApplications'],
    queryFn: async () => {
      try {
        return await applicationsAPI.getReceivedApplications();
      } catch (error) {
        console.error('Error fetching applications:', error);
        return { data: [] };
      }
    },

    refetchInterval: 2000,
  });

  const applications = applicationsData?.data || [];

  // Group applications by status
  const groupedApplications = {
    all: applications,
    submitted: applications?.filter(app => app?.status === APPLICATION_STATUS.SUBMITTED) || [],
    under_review: applications?.filter(app => app?.status === APPLICATION_STATUS.UNDER_REVIEW) || [],
    shortlisted: applications?.filter(app => app?.status === APPLICATION_STATUS.SHORTLISTED) || [],
    interview_scheduled: applications?.filter(app => app?.status === APPLICATION_STATUS.INTERVIEW_SCHEDULED) || [],
    accepted: applications?.filter(app => app?.status === APPLICATION_STATUS.ACCEPTED) || [],
    rejected: applications?.filter(app => app?.status === APPLICATION_STATUS.REJECTED) || [],
  };

  // Update status mutation
  const updateStatusMutation = useMutation({
    mutationFn: ({ applicationId, data }) => applicationsAPI.updateStatus(applicationId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['providerApplications']);
      queryClient.invalidateQueries(['providerStats']);
      toast({
        title: "Status Updated",
        description: "Application status has been updated successfully.",
      });
      setSelectedApplication(null);
      setIsUpdating(false);
      setUpdateForm({ status: '', notes: '' });
    },
    onError: (error) => {
      toast({
        title: "Update Failed",
        description: error?.message || "Failed to update status. Please try again.",
        variant: "destructive",
      });
    },
  });

  const handleViewDetails = (application) => {
    setSelectedApplication(application);
    setIsUpdating(false);
  };

  const handleUpdateStatus = (application) => {
      // Debug log
        console.log('Attempting to update application:', {
          id: application?.applicationId,
          status: application?.status,
          learner: application?.learner?.firstName
        });

        // Validate application has ID
        if (!application?.applicationId) {
          console.error('Application missing ID:', application);
          toast({
            title: "Error",
            description: "Application ID is missing. Please refresh the page and try again.",
            variant: "destructive",
          });
          return;
        }

    setSelectedApplication(application);
    setUpdateForm({
      status: application?.status || '',
      notes: application?.providerNotes || '',
    });
    setIsUpdating(true);
  };

  const handleSubmitUpdate = () => {
      // Validate application ID
        if (!selectedApplication?.applicationId) {
          console.error('No application selected or missing ID');
          toast({
            title: "Error",
            description: "Invalid application. Please close and try again.",
            variant: "destructive",
          });
          return;
        }

    if (!updateForm.status) {
      toast({
        title: "Invalid Input",
        description: "Please select a status.",
        variant: "destructive",
      });
      return;
    }

// Debug log before mutation
  console.log('Submitting update:', {
    applicationId: selectedApplication.applicationId,
    newStatus: updateForm.status,
    notes: updateForm.notes
  });


    updateStatusMutation.mutate({
      applicationId: selectedApplication?.applicationId,
      data: {
        status: updateForm.status,
        providerNotes: updateForm.notes,
      },
    });
  };

  const getStatusBadgeClass = (status) => {
    return STATUS_COLORS[status] || 'bg-gray-100 text-gray-800';
  };

  // Application Card Component
  const ApplicationCard = ({ application }) => {
    const learner = application?.learner;
    const bursary = application?.bursary;

    return (
      <Card className="hover:shadow-md transition-shadow">
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <CardTitle className="text-lg">
                {learner?.firstName || ''} {learner?.lastName || ''}
              </CardTitle>
              <CardDescription className="mt-1">
                {learner?.schoolName || 'Unknown School'} • {learner?.location || 'Unknown Location'}
              </CardDescription>
            </div>
            <Badge className={getStatusBadgeClass(application?.status)}>
              {application?.status?.replace(/_/g, ' ').toUpperCase() || 'UNKNOWN'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Bursary Info */}
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-sm">
              <FileText className="h-4 w-4 text-muted-foreground" />
              <span className="font-medium">{bursary?.title || 'Unknown Bursary'}</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <DollarSign className="h-4 w-4 text-muted-foreground" />
              <span className="text-green-600 font-semibold">
                {bursary?.amount ? formatCurrency(bursary.amount) : 'N/A'}
              </span>
            </div>
          </div>

          {/* Learner Info */}
          <div className="grid grid-cols-2 gap-2 text-sm">
            <div>
              <span className="text-muted-foreground">Household Income:</span>
              <p className="font-medium">
                {learner?.householdIncome ? formatCurrency(learner.householdIncome) : 'N/A'}
              </p>
            </div>
            <div>
              <span className="text-muted-foreground">Applied:</span>
              <p className="font-medium">
                {application?.appliedAt ? formatRelativeTime(application.appliedAt) : 'N/A'}
              </p>
            </div>
          </div>

          {/* Provider Notes */}
          {application?.providerNotes && (
            <div className="bg-muted p-3 rounded-lg">
              <p className="text-sm font-medium mb-1">Your Notes:</p>
              <p className="text-sm text-muted-foreground">{application.providerNotes}</p>
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-2 pt-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => handleViewDetails(application)}
            >
              <Eye className="mr-2 h-4 w-4" />
              View Details
            </Button>
            <Button
              className="flex-1"
              onClick={() => handleUpdateStatus(application)}
            >
              <Edit className="mr-2 h-4 w-4" />
              Update Status
            </Button>
          </div>
        </CardContent>
      </Card>
    );
  };

  // Loading State
  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Applications Received</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[...Array(4)].map((_, i) => (
            <Skeleton key={i} className="h-64" />
          ))}
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Applications Received</h1>
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
        <h1 className="text-3xl font-bold">Applications Received</h1>
        <p className="text-muted-foreground">
          Review and manage bursary applications
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-2 md:grid-cols-7 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Total</CardDescription>
            <CardTitle className="text-2xl">{groupedApplications?.all?.length || 0}</CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Submitted</CardDescription>
            <CardTitle className="text-2xl text-blue-600">
              {groupedApplications?.submitted?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Under Review</CardDescription>
            <CardTitle className="text-2xl text-yellow-600">
              {groupedApplications?.under_review?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Shortlisted</CardDescription>
            <CardTitle className="text-2xl text-purple-600">
              {groupedApplications?.shortlisted?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Interview</CardDescription>
            <CardTitle className="text-2xl text-indigo-600">
              {groupedApplications?.interview_scheduled?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Accepted</CardDescription>
            <CardTitle className="text-2xl text-green-600">
              {groupedApplications?.accepted?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Rejected</CardDescription>
            <CardTitle className="text-2xl text-red-600">
              {groupedApplications?.rejected?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs defaultValue="all" className="w-full">
        <TabsList className="grid w-full grid-cols-7">
          <TabsTrigger value="all">
            All ({groupedApplications?.all?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="submitted">
            Submitted ({groupedApplications?.submitted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="under_review">
            Review ({groupedApplications?.under_review?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="shortlisted">
            Shortlist ({groupedApplications?.shortlisted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="interview_scheduled">
            Interview ({groupedApplications?.interview_scheduled?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="accepted">
            Accepted ({groupedApplications?.accepted?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="rejected">
            Rejected ({groupedApplications?.rejected?.length || 0})
          </TabsTrigger>
        </TabsList>

        {['all', 'submitted', 'under_review', 'shortlisted', 'interview_scheduled', 'accepted', 'rejected'].map(status => (
          <TabsContent key={status} value={status} className="mt-6">
            {groupedApplications[status]?.length === 0 ? (
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                  <p className="text-muted-foreground">
                    No applications in this category
                  </p>
                </CardContent>
              </Card>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {groupedApplications[status]?.map((application) => (
                  <ApplicationCard key={application?.applicationId} application={application} />
                ))}
              </div>
            )}
          </TabsContent>
        ))}
      </Tabs>

      {/* View Details Dialog */}
      <Dialog open={!!selectedApplication && !isUpdating} onOpenChange={() => setSelectedApplication(null)}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          {selectedApplication && (
            <>
              <DialogHeader>
                <DialogTitle className="text-2xl">Application Details</DialogTitle>
                <DialogDescription>
                  Full application information
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-6 py-4">
                {/* Learner Info */}
                <div className="space-y-3">
                  <h3 className="font-semibold flex items-center gap-2">
                    <User className="h-4 w-4" />
                    Learner Information
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Name:</span>
                      <p className="font-medium">
                        {selectedApplication?.learner?.firstName || ''} {selectedApplication?.learner?.lastName || ''}
                      </p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Email:</span>
                      <p className="font-medium">{selectedApplication?.learner?.email || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">School:</span>
                      <p className="font-medium">{selectedApplication?.learner?.schoolName || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Location:</span>
                      <p className="font-medium">{selectedApplication?.learner?.location || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Household Income:</span>
                      <p className="font-medium">
                        {selectedApplication?.learner?.householdIncome
                          ? formatCurrency(selectedApplication.learner.householdIncome)
                          : 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Bursary Info */}
                <div className="space-y-3">
                  <h3 className="font-semibold flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    Bursary Details
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Bursary:</span>
                      <p className="font-medium">{selectedApplication?.bursary?.title || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Amount:</span>
                      <p className="font-medium text-green-600">
                        {selectedApplication?.bursary?.amount
                          ? formatCurrency(selectedApplication.bursary.amount)
                          : 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Application Info */}
                <div className="space-y-3">
                  <h3 className="font-semibold flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Application Status
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Status:</span>
                      <div className="mt-1">
                        <Badge className={getStatusBadgeClass(selectedApplication?.status)}>
                          {selectedApplication?.status?.replace(/_/g, ' ').toUpperCase() || 'UNKNOWN'}
                        </Badge>
                      </div>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Applied:</span>
                      <p className="font-medium">
                        {selectedApplication?.submittedAt ? formatDate(selectedApplication.submittedAt) : 'N/A'}
                      </p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Last Updated:</span>
                      <p className="font-medium">
                        {selectedApplication?.reviewedAt ? formatDate(selectedApplication.reviewedAt) : 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Provider Notes */}
                {selectedApplication?.notes && (
                  <div className="space-y-2">
                    <h3 className="font-semibold">Your Notes</h3>
                    <div className="bg-muted p-4 rounded-lg">
                      <p className="text-sm">{selectedApplication.notes}</p>
                    </div>
                  </div>
                )}
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setSelectedApplication(null)}>
                  Close
                </Button>
                <Button onClick={() => handleUpdateStatus(selectedApplication)}>
                  Update Status
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* Update Status Dialog */}
      <Dialog open={!!selectedApplication && isUpdating} onOpenChange={() => {
        setSelectedApplication(null);
        setIsUpdating(false);
      }}>
        <DialogContent className="max-w-md">
          {selectedApplication && (
            <>
              <DialogHeader>
                <DialogTitle>Update Application Status</DialogTitle>
                <DialogDescription>
                  {selectedApplication?.learner?.firstName || ''} {selectedApplication?.learner?.lastName || ''} - {selectedApplication?.bursary?.title || ''}
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label>Status</Label>
                  <Select
                    value={updateForm.status}
                    onValueChange={(value) => setUpdateForm({ ...updateForm, status: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select status" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value={APPLICATION_STATUS.SUBMITTED}>Submitted</SelectItem>
                      <SelectItem value={APPLICATION_STATUS.UNDER_REVIEW}>Under Review</SelectItem>
                      <SelectItem value={APPLICATION_STATUS.SHORTLISTED}>Shortlisted</SelectItem>
                      <SelectItem value={APPLICATION_STATUS.INTERVIEW_SCHEDULED}>Interview Scheduled</SelectItem>
                      <SelectItem value={APPLICATION_STATUS.ACCEPTED}>Accepted</SelectItem>
                      <SelectItem value={APPLICATION_STATUS.REJECTED}>Rejected</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Notes (Optional)</Label>
                  <Textarea
                    placeholder="Add notes about this application..."
                    value={updateForm.notes}
                    onChange={(e) => setUpdateForm({ ...updateForm, notes: e.target.value })}
                    rows={4}
                  />
                </div>
              </div>

              <DialogFooter>
                <Button
                  variant="outline"
                  onClick={() => {
                    setSelectedApplication(null);
                    setIsUpdating(false);
                  }}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleSubmitUpdate}
                  disabled={updateStatusMutation.isPending}
                >
                  {updateStatusMutation.isPending ? 'Updating...' : 'Update Status'}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default ProviderApplications;