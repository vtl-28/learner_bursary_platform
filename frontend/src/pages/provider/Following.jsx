import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import { Users, TrendingUp, MapPin, DollarSign, GraduationCap, Eye, UserMinus } from 'lucide-react';
import { followAPI } from '@/api/follow';
import { providerSearchAPI } from '@/api/providerSearch';
import { formatCurrency, formatRelativeTime } from '@/utils/helpers';

function Following() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [selectedLearner, setSelectedLearner] = useState(null);

  // Fetch following list
  const { data: followingData, isLoading, error } = useQuery({
    queryKey: ['providerFollowing'],
    queryFn: async () => {
      try {
        return await followAPI.getFollowing();
      } catch (error) {
        console.error('Error fetching following:', error);
        return { data: [] };
      }
    },
    refetchInterval: 2000,
  });

  const following = followingData?.data || [];
  console.log(following);

  // Unfollow mutation
  const unfollowMutation = useMutation({
    mutationFn: (learnerId) => followAPI.unfollowLearner(learnerId),
    onSuccess: () => {
      queryClient.invalidateQueries(['providerFollowing']);
      queryClient.invalidateQueries(['followingCount']);
      toast({
        title: "Learner Unfollowed",
        description: "You are no longer following this learner.",
      });
    },
    onError: (error) => {
      toast({
        title: "Unfollow Failed",
        description: error?.message || "Failed to unfollow learner.",
        variant: "destructive",
      });
    },
  });

  const handleViewProfile = async (learnerId) => {
    try {
      const response = await providerSearchAPI.getLearnerProfile(learnerId);
      setSelectedLearner(response?.data);
    } catch (error) {
      console.error('Error fetching profile:', error);
      toast({
        title: "Failed to Load Profile",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    }
  };

  const handleUnfollow = (learnerId) => {
    if (window.confirm('Are you sure you want to unfollow this learner?')) {
      unfollowMutation.mutate(learnerId);
    }
  };

  // Learner Card Component
  const LearnerCard = ({ learner }) => {
    return (
      <Card className="hover:shadow-md transition-shadow">
        <CardHeader>
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <CardTitle className="text-lg">
                {learner?.firstName || ''} {learner?.lastName || ''}
              </CardTitle>
              <CardDescription className="mt-1">
                {learner?.schoolName || 'Unknown School'}
              </CardDescription>
            </div>
            <Badge variant="secondary">
              Following since {learner?.followedAt ? formatRelativeTime(learner.followedAt) : 'N/A'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Key Stats */}
          <div className="grid grid-cols-2 gap-4">
            <div className="flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-green-600" />
              <div>
                <p className="text-xs text-muted-foreground">Average</p>
                <p className="text-lg font-bold text-green-600">
                  {learner?.overallAverage?.toFixed(1) || 0}%
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="h-4 w-4 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Location</p>
                <p className="text-sm font-medium">{learner?.location || 'N/A'}</p>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2 text-sm">
            <DollarSign className="h-4 w-4 text-muted-foreground" />
            <span className="text-muted-foreground">Household Income:</span>
            <span className="font-medium">
              {learner?.householdIncome ? formatCurrency(learner.householdIncome) : 'N/A'}
            </span>
          </div>

          {/* Actions */}
          <div className="flex gap-2 pt-2">
            <Button
              variant="outline"
              className="flex-1"
              onClick={() => handleViewProfile(learner?.id)}
            >
              <Eye className="mr-2 h-4 w-4" />
              View Profile
            </Button>
            <Button
              variant="destructive"
              className="flex-1"
              onClick={() => handleUnfollow(learner?.id)}
              disabled={unfollowMutation.isPending}
            >
              <UserMinus className="mr-2 h-4 w-4" />
              Unfollow
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
        <h1 className="text-3xl font-bold">Following</h1>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
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
        <h1 className="text-3xl font-bold">Following</h1>
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load following list. Please try refreshing the page.
          </AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Following</h1>
          <p className="text-muted-foreground">
            Learners you are tracking
          </p>
        </div>
        <Badge variant="secondary" className="text-lg px-4 py-2">
          {following?.length || 0} learners
        </Badge>
      </div>

      {/* Following List */}
      {following?.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <Users className="h-12 w-12 text-muted-foreground mb-4" />
            <p className="text-muted-foreground mb-2">Not following anyone yet</p>
            <p className="text-sm text-muted-foreground text-center mb-4">
              Search for high-performing learners to follow and track their progress
            </p>
            <Button onClick={() => window.location.href = '/provider/search'}>
              Search Learners
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {following?.map((learner) => (
            <LearnerCard key={learner?.id} learner={learner} />
          ))}
        </div>
      )}

      {/* Profile Dialog (same as SearchLearners) */}
      <Dialog open={!!selectedLearner} onOpenChange={() => setSelectedLearner(null)}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          {selectedLearner && (
            <>
              <DialogHeader>
                <DialogTitle className="text-2xl">
                  {selectedLearner?.firstName || ''} {selectedLearner?.lastName || ''}
                </DialogTitle>
                <DialogDescription>
                  Complete learner profile and academic history
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-6 py-4">
                {/* Personal Info */}
                <div className="space-y-3">
                  <h3 className="font-semibold flex items-center gap-2">
                    <Users className="h-4 w-4" />
                    Personal Information
                  </h3>
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Email:</span>
                      <p className="font-medium">{selectedLearner?.email || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">School:</span>
                      <p className="font-medium">{selectedLearner?.schoolName || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Location:</span>
                      <p className="font-medium">{selectedLearner?.location || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Household Income:</span>
                      <p className="font-medium">
                        {selectedLearner?.householdIncome
                          ? formatCurrency(selectedLearner.householdIncome)
                          : 'N/A'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Academic Performance */}
                <div className="space-y-3">
                  <h3 className="font-semibold flex items-center gap-2">
                    <GraduationCap className="h-4 w-4" />
                    Academic Performance
                  </h3>
                  <div className="bg-primary/10 p-4 rounded-lg">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Overall Average</span>
                      <span className="text-2xl font-bold text-green-600">
                        {selectedLearner?.overallAverage?.toFixed(1) || 0}%
                      </span>
                    </div>
                  </div>

                  {/* Academic Years */}
                  {selectedLearner?.academicYears && selectedLearner.academicYears.length > 0 ? (
                    <div className="space-y-4">
                      {selectedLearner.academicYears.map((year) => (
                        <Card key={year?.id}>
                          <CardHeader className="pb-3">
                            <CardTitle className="text-base">
                              {year?.year || 'N/A'} - Grade {year?.gradeLevel || 'N/A'}
                            </CardTitle>
                          </CardHeader>
                          <CardContent>
                            {year?.terms && year.terms.length > 0 ? (
                              <div className="space-y-3">
                                {year.terms.map((term) => (
                                  <div key={term?.id} className="border-l-2 border-primary pl-3">
                                    <div className="flex items-center justify-between mb-2">
                                      <span className="font-medium text-sm">
                                        Term {term?.termNumber || 'N/A'}
                                      </span>
                                      <Badge variant="secondary">
                                        Avg: {term?.averageMark?.toFixed(1) || 0}%
                                      </Badge>
                                    </div>
                                    <div className="grid grid-cols-2 gap-2 text-sm">
                                      {term?.subjects?.map((subject) => (
                                        <div key={subject?.id} className="flex justify-between">
                                          <span className="text-muted-foreground">
                                            {subject?.subjectName || 'N/A'}
                                          </span>
                                          <span className="font-medium">
                                            {subject?.mark || 0}%
                                          </span>
                                        </div>
                                      ))}
                                    </div>
                                  </div>
                                ))}
                              </div>
                            ) : (
                              <p className="text-sm text-muted-foreground">No terms recorded</p>
                            )}
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-muted-foreground">No academic records available</p>
                  )}
                </div>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setSelectedLearner(null)}>
                  Close
                </Button>
                <Button
                  variant="destructive"
                  onClick={() => {
                    handleUnfollow(selectedLearner?.id);
                    setSelectedLearner(null);
                  }}
                  disabled={unfollowMutation.isPending}
                >
                  <UserMinus className="mr-2 h-4 w-4" />
                  Unfollow
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default Following;