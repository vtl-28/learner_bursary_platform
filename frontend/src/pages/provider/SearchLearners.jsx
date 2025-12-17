import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { useToast } from '@/hooks/use-toast';
import { Search, User, GraduationCap, MapPin, DollarSign, TrendingUp, UserPlus, UserCheck, Eye } from 'lucide-react';
import { providerSearchAPI } from '@/api/providerSearch';
import { learnerAPI } from '@/api/learner';
import { followAPI } from '@/api/follow';
import { formatCurrency } from '@/utils/helpers';

function SearchLearners() {
  const queryClient = useQueryClient();
  const { toast } = useToast();

  const [searchParams, setSearchParams] = useState({
    minAverage: '',
    location: '',
    schoolName: '',
  });

  const [selectedLearner, setSelectedLearner] = useState(null);
  const [searchResults, setSearchResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);

  // Follow mutation
  const followMutation = useMutation({
    mutationFn: (learnerId) => followAPI.followLearner(learnerId),
    onSuccess: () => {
      queryClient.invalidateQueries(['searchResults']);
      queryClient.invalidateQueries(['followingCount']);
      toast({
        title: "Learner Followed",
        description: "You are now following this learner.",
      });
    },
    onError: (error) => {
      toast({
        title: "Follow Failed",
        description: error?.message || "Failed to follow learner. Please try again.",
        variant: "destructive",
      });
    },
  });

  // Unfollow mutation
  const unfollowMutation = useMutation({
    mutationFn: (learnerId) => followAPI.unfollowLearner(learnerId),
    onSuccess: () => {
      queryClient.invalidateQueries(['searchResults']);
      queryClient.invalidateQueries(['followingCount']);
      toast({
        title: "Learner Unfollowed",
        description: "You are no longer following this learner.",
      });
    },
    onError: (error) => {
      toast({
        title: "Unfollow Failed",
        description: error?.message || "Failed to unfollow learner. Please try again.",
        variant: "destructive",
      });
    },
  });

  const handleSearch = async () => {
    try {
      const params = {};
      if (searchParams.minAverage) params.minAverage = parseFloat(searchParams.minAverage);
      if (searchParams.location) params.location = searchParams.location;
      if (searchParams.schoolName) params.schoolName = searchParams.schoolName;

      const response = await providerSearchAPI.searchLearners(params);
      setSearchResults(response?.data || []);
      setHasSearched(true);
    } catch (error) {
      console.error('Search error:', error);
      toast({
        title: "Search Failed",
        description: error?.message || "Failed to search learners.",
        variant: "destructive",
      });
      setSearchResults([]);
      setHasSearched(true);
    }
  };

  const handleViewProfile = async (learnerId) => {
    try {
      const response = await providerSearchAPI.getLearnerProfile(learnerId);
      console.log(response?.data);
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

  const handleFollow = (learnerId) => {
    followMutation.mutate(learnerId);
  };

  const handleUnfollow = (learnerId) => {
    unfollowMutation.mutate(learnerId);
  };

  const clearSearch = () => {
    setSearchParams({
      minAverage: '',
      location: '',
      schoolName: '',
    });
    setSearchResults([]);
    setHasSearched(false);
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
            {learner?.isFollowing && (
              <Badge variant="secondary">
                <UserCheck className="mr-1 h-3 w-3" />
                Following
              </Badge>
            )}
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
              onClick={() => handleViewProfile(learner?.learnerId)}
            >
              <Eye className="mr-2 h-4 w-4" />
              View Profile
            </Button>
            {learner?.isFollowing ? (
              <Button
                variant="secondary"
                className="flex-1"
                onClick={() => handleUnfollow(learner?.learnerId)}
                disabled={unfollowMutation.isPending}
              >
                <UserCheck className="mr-2 h-4 w-4" />
                Unfollow
              </Button>
            ) : (
              <Button
                className="flex-1"
                onClick={() => handleFollow(learner?.learnerId)}
                disabled={followMutation.isPending}
              >
                <UserPlus className="mr-2 h-4 w-4" />
                Follow
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">Search Learners</h1>
        <p className="text-muted-foreground">
          Find and follow high-performing learners
        </p>
      </div>

      {/* Search Form */}
      <Card>
        <CardHeader>
          <CardTitle>Search Criteria</CardTitle>
          <CardDescription>Filter learners by performance and location</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label>Minimum Average (%)</Label>
              <Input
                type="number"
                placeholder="75"
                min="0"
                max="100"
                value={searchParams.minAverage}
                onChange={(e) => setSearchParams({ ...searchParams, minAverage: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>Location</Label>
              <Input
                placeholder="Johannesburg, Gauteng"
                value={searchParams.location}
                onChange={(e) => setSearchParams({ ...searchParams, location: e.target.value })}
              />
            </div>
            <div className="space-y-2">
              <Label>School Name</Label>
              <Input
                placeholder="Greenside High"
                value={searchParams.schoolName}
                onChange={(e) => setSearchParams({ ...searchParams, schoolName: e.target.value })}
              />
            </div>
          </div>
          <div className="flex gap-2 mt-4">
            <Button onClick={handleSearch} className="flex-1">
              <Search className="mr-2 h-4 w-4" />
              Search
            </Button>
            {hasSearched && (
              <Button variant="outline" onClick={clearSearch}>
                Clear
              </Button>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Results */}
      {hasSearched && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">
              Search Results
            </h2>
            <Badge variant="secondary">
              {searchResults?.length || 0} learners found
            </Badge>
          </div>

          {searchResults?.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Search className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-2">No learners found</p>
                <p className="text-sm text-muted-foreground text-center">
                  Try adjusting your search criteria
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {searchResults?.map((learner) => (
                <LearnerCard key={learner?.learnerId} learner={learner} />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Profile Dialog */}
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
                    <User className="h-4 w-4" />
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
                        {selectedLearner?.averageMark?.toFixed(1) || 0}%
                      </span>
                    </div>
                  </div>

                  {/* Academic Years */}
                  {selectedLearner?.academicHistory && selectedLearner?.academicHistory?.length > 0 ? (
                    <div className="space-y-4">
                      {selectedLearner?.academicHistory.map((year) => (
                        <Card key={year?.id}>
                          <CardHeader className="pb-3">
                            <CardTitle className="text-base">
                              {year?.year || 'N/A'} - Grade {year?.gradeLevel || 'N/A'}
                            </CardTitle>
                          </CardHeader>
                          <CardContent>
                            {year?.terms && year?.terms?.length > 0 ? (
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
                {selectedLearner?.isFollowing ? (
                  <Button
                    variant="secondary"
                    onClick={() => {
                      handleUnfollow(selectedLearner?.learnerId);
                      setSelectedLearner(null);
                    }}
                    disabled={unfollowMutation.isPending}
                  >
                    <UserCheck className="mr-2 h-4 w-4" />
                    Unfollow
                  </Button>
                ) : (
                  <Button
                    onClick={() => {
                      handleFollow(selectedLearner?.learnerId);
                      setSelectedLearner(null);
                    }}
                    disabled={followMutation.isPending}
                  >
                    <UserPlus className="mr-2 h-4 w-4" />
                    Follow Learner
                  </Button>
                )}
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default SearchLearners;