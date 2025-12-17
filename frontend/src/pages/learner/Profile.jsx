import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { useToast } from '@/hooks/use-toast';
import { User, GraduationCap, Edit2, Save, X, Plus, Trash2 } from 'lucide-react';
import { learnerAPI } from '@/api/learner';
import { academicAPI } from '@/api/academic';
import { followAPI } from '@/api/follow';
import { formatCurrency, calculateAverage, formatRelativeTime } from '@/utils/helpers';
import { GRADE_LEVELS, TERM_NUMBERS, COMMON_SUBJECTS } from '@/utils/constants';
import useAuthStore from '@/lib/stores/authStore';

function Profile() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const { user: authUser } = useAuthStore();

  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({});

  const [isAddingYear, setIsAddingYear] = useState(false);
  const [newYear, setNewYear] = useState({ year: '', gradeLevel: '' });

  const [isAddingTerm, setIsAddingTerm] = useState(null); // academicYearId
  const [newTerm, setNewTerm] = useState({ termNumber: '', subjects: [] });

  const [newSubject, setNewSubject] = useState({ subjectName: '', mark: '' });

  // Fetch profile
  const { data: profileData, isLoading: profileLoading } = useQuery({
    queryKey: ['learnerProfile'],
    queryFn: async () => {
      try {
        return await learnerAPI.getProfile();
      } catch (error) {
        console.error('Error fetching profile:', error);
        return { data: null };
      }
    },
  });

  const profile = profileData?.data;

  // Fetch academic results
  const { data: academicData, isLoading: academicLoading } = useQuery({
    queryKey: ['myAcademicResults'],
    queryFn: async () => {
      try {
        return await academicAPI.getMyResults();
      } catch (error) {
        console.error('Error fetching academic results:', error);
        return { data: [] };
      }
    },
  });

  const academicYears = academicData?.data || [];

  // Fetch followers
  const { data: followersData } = useQuery({
    queryKey: ['myFollowers'],
    queryFn: async () => {
      try {
        return await followAPI.getMyFollowers();
      } catch (error) {
        console.error('Error fetching followers:', error);
        return { data: [] };
      }
    },
  });

  const followers = followersData?.data || [];

  // Update profile mutation
  const updateProfileMutation = useMutation({
    mutationFn: (data) => learnerAPI.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['learnerProfile']);
      toast({
        title: "Profile Updated",
        description: "Your profile has been updated successfully.",
      });
      setIsEditingProfile(false);
    },
    onError: (error) => {
      toast({
        title: "Update Failed",
        description: error?.message || "Failed to update profile. Please try again.",
        variant: "destructive",
      });
    },
  });

  // Create academic year mutation
  const createYearMutation = useMutation({
    mutationFn: (data) => academicAPI.createYear(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['myAcademicResults']);
      toast({
        title: "Academic Year Added",
        description: "Academic year has been added successfully.",
      });
      setIsAddingYear(false);
      setNewYear({ year: '', gradeLevel: '' });
    },
    onError: (error) => {
      toast({
        title: "Failed to Add Year",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    },
  });

  // Add term mutation
  const addTermMutation = useMutation({
    mutationFn: ({ academicYearId, data }) => academicAPI.addTerm(academicYearId, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['myAcademicResults']);
      toast({
        title: "Term Added",
        description: "Term results have been added successfully.",
      });
      setIsAddingTerm(null);
      setNewTerm({ termNumber: '', subjects: [] });
      setNewSubject({ subjectName: '', mark: '' });
    },
    onError: (error) => {
      toast({
        title: "Failed to Add Term",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    },
  });

  // Delete academic year mutation
  const deleteYearMutation = useMutation({
    mutationFn: (yearId) => academicAPI.deleteYear(yearId),
    onSuccess: () => {
      queryClient.invalidateQueries(['myAcademicResults']);
      toast({
        title: "Academic Year Deleted",
        description: "Academic year has been deleted successfully.",
      });
    },
    onError: (error) => {
      toast({
        title: "Delete Failed",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    },
  });

  // Handlers
  const handleEditProfile = () => {
    setProfileForm({
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || '',
      schoolName: profile?.schoolName || '',
      location: profile?.location || '',
      householdIncome: profile?.householdIncome || '',
    });
    setIsEditingProfile(true);
  };

  const handleSaveProfile = () => {
    const data = {
      ...profileForm,
      householdIncome: parseFloat(profileForm.householdIncome),
    };
    updateProfileMutation.mutate(data);
  };

  const handleAddYear = () => {
    if (!newYear.year || !newYear.gradeLevel) {
      toast({
        title: "Invalid Input",
        description: "Please provide both year and grade level.",
        variant: "destructive",
      });
      return;
    }

    createYearMutation.mutate({
      year: parseInt(newYear.year),
      gradeLevel: parseInt(newYear.gradeLevel),
    });
  };

  const handleAddSubject = () => {
    if (!newSubject.subjectName || !newSubject.mark) {
      toast({
        title: "Invalid Input",
        description: "Please provide subject name and mark.",
        variant: "destructive",
      });
      return;
    }

    setNewTerm(prev => ({
      ...prev,
      subjects: [
        ...prev.subjects,
        {
          subjectName: newSubject.subjectName,
          mark: parseFloat(newSubject.mark),
        },
      ],
    }));

    setNewSubject({ subjectName: '', mark: '' });
  };

  const handleRemoveSubject = (index) => {
    setNewTerm(prev => ({
      ...prev,
      subjects: prev.subjects.filter((_, i) => i !== index),
    }));
  };

  const handleAddTerm = (academicYearId) => {
    if (!newTerm.termNumber || newTerm.subjects.length === 0) {
      toast({
        title: "Invalid Input",
        description: "Please provide term number and at least one subject.",
        variant: "destructive",
      });
      return;
    }

    addTermMutation.mutate({
      academicYearId,
      data: {
        termNumber: parseInt(newTerm.termNumber),
        subjects: newTerm.subjects,
      },
    });
  };

  const handleDeleteYear = (yearId) => {
    if (window.confirm('Are you sure you want to delete this academic year? This will delete all terms and subjects.')) {
      deleteYearMutation.mutate(yearId);
    }
  };

  if (profileLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64" />
        <Skeleton className="h-96 w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold">My Profile</h1>
        <p className="text-muted-foreground">
          Manage your personal information and academic records
        </p>
      </div>

      <Tabs defaultValue="profile" className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="profile">
            <User className="mr-2 h-4 w-4" />
            Personal Info
          </TabsTrigger>
          <TabsTrigger value="academic">
            <GraduationCap className="mr-2 h-4 w-4" />
            Academic Results
          </TabsTrigger>
          <TabsTrigger value="followers">
            Followers ({followers?.length || 0})
          </TabsTrigger>
        </TabsList>

        {/* Personal Info Tab */}
        <TabsContent value="profile" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Personal Information</CardTitle>
                  <CardDescription>Your basic profile details</CardDescription>
                </div>
                {!isEditingProfile ? (
                  <Button variant="outline" size="sm" onClick={handleEditProfile}>
                    <Edit2 className="mr-2 h-4 w-4" />
                    Edit
                  </Button>
                ) : (
                  <div className="flex gap-2">
                    <Button variant="outline" size="sm" onClick={() => setIsEditingProfile(false)}>
                      <X className="mr-2 h-4 w-4" />
                      Cancel
                    </Button>
                    <Button size="sm" onClick={handleSaveProfile} disabled={updateProfileMutation.isPending}>
                      <Save className="mr-2 h-4 w-4" />
                      {updateProfileMutation.isPending ? 'Saving...' : 'Save'}
                    </Button>
                  </div>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {!isEditingProfile ? (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <Label className="text-muted-foreground">First Name</Label>
                    <p className="text-lg font-medium">{profile?.firstName || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Last Name</Label>
                    <p className="text-lg font-medium">{profile?.lastName || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Email</Label>
                    <p className="text-lg font-medium">{profile?.email || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">School</Label>
                    <p className="text-lg font-medium">{profile?.schoolName || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Location</Label>
                    <p className="text-lg font-medium">{profile?.location || 'N/A'}</p>
                  </div>
                  <div>
                    <Label className="text-muted-foreground">Household Income</Label>
                    <p className="text-lg font-medium">
                      {profile?.householdIncome ? formatCurrency(profile.householdIncome) : 'N/A'}
                    </p>
                  </div>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label>First Name</Label>
                    <Input
                      value={profileForm.firstName}
                      onChange={(e) => setProfileForm({ ...profileForm, firstName: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Last Name</Label>
                    <Input
                      value={profileForm.lastName}
                      onChange={(e) => setProfileForm({ ...profileForm, lastName: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>School Name</Label>
                    <Input
                      value={profileForm.schoolName}
                      onChange={(e) => setProfileForm({ ...profileForm, schoolName: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Location</Label>
                    <Input
                      value={profileForm.location}
                      onChange={(e) => setProfileForm({ ...profileForm, location: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2 md:col-span-2">
                    <Label>Household Income (ZAR)</Label>
                    <Input
                      type="number"
                      value={profileForm.householdIncome}
                      onChange={(e) => setProfileForm({ ...profileForm, householdIncome: e.target.value })}
                    />
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Academic Results Tab */}
        <TabsContent value="academic" className="space-y-6">
          {/* Add Year Button */}
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-lg font-semibold">Academic History</h3>
              <p className="text-sm text-muted-foreground">
                Your grades and performance records
              </p>
            </div>
            <Button onClick={() => setIsAddingYear(true)} disabled={isAddingYear}>
              <Plus className="mr-2 h-4 w-4" />
              Add Academic Year
            </Button>
          </div>

          {/* Add Year Form */}
          {isAddingYear && (
            <Card className="border-primary">
              <CardHeader>
                <CardTitle>Add Academic Year</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Year</Label>
                    <Input
                      type="number"
                      placeholder="2024"
                      value={newYear.year}
                      onChange={(e) => setNewYear({ ...newYear, year: e.target.value })}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Grade Level</Label>
                    <select
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                      value={newYear.gradeLevel}
                      onChange={(e) => setNewYear({ ...newYear, gradeLevel: e.target.value })}
                    >
                      <option value="">Select Grade</option>
                      {GRADE_LEVELS.map(grade => (
                        <option key={grade} value={grade}>Grade {grade}</option>
                      ))}
                    </select>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button onClick={handleAddYear} disabled={createYearMutation.isPending}>
                    {createYearMutation.isPending ? 'Adding...' : 'Add Year'}
                  </Button>
                  <Button variant="outline" onClick={() => {
                    setIsAddingYear(false);
                    setNewYear({ year: '', gradeLevel: '' });
                  }}>
                    Cancel
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Academic Years List */}
          {academicLoading ? (
            <div className="space-y-4">
              {[...Array(2)].map((_, i) => (
                <Skeleton key={i} className="h-48 w-full" />
              ))}
            </div>
          ) : academicYears?.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <GraduationCap className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-4">No academic records yet</p>
                <Button onClick={() => setIsAddingYear(true)}>
                  <Plus className="mr-2 h-4 w-4" />
                  Add Your First Year
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-6">
              {academicYears?.map((year) => (
                <Card key={year?.id}>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div>
                        <CardTitle>
                          {year?.year || 'N/A'} - Grade {year?.gradeLevel || 'N/A'}
                        </CardTitle>
                        <CardDescription>
                          {year?.terms?.length || 0} term(s) recorded
                        </CardDescription>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setIsAddingTerm(year?.id)}
                        >
                          <Plus className="mr-2 h-4 w-4" />
                          Add Term
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDeleteYear(year?.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    {/* Add Term Form */}
                    {isAddingTerm === year?.id && (
                      <Card className="border-primary">
                        <CardHeader>
                          <CardTitle className="text-base">Add Term Results</CardTitle>
                        </CardHeader>
                        <CardContent className="space-y-4">
                          <div className="space-y-2">
                            <Label>Term Number</Label>
                            <select
                              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                              value={newTerm.termNumber}
                              onChange={(e) => setNewTerm({ ...newTerm, termNumber: e.target.value })}
                            >
                              <option value="">Select Term</option>
                              {TERM_NUMBERS.map(term => (
                                <option key={term} value={term}>Term {term}</option>
                              ))}
                            </select>
                          </div>

                          {/* Add Subject */}
                          <div className="space-y-2">
                            <Label>Add Subjects</Label>
                            <div className="flex gap-2">
                              <Input
                                placeholder="Subject name"
                                value={newSubject.subjectName}
                                onChange={(e) => setNewSubject({ ...newSubject, subjectName: e.target.value })}
                              />
                              <Input
                                type="number"
                                placeholder="Mark (0-100)"
                                value={newSubject.mark}
                                onChange={(e) => setNewSubject({ ...newSubject, mark: e.target.value })}
                                className="w-32"
                              />
                              <Button onClick={handleAddSubject} size="sm">
                                <Plus className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>

                          {/* Subjects List */}
                          {newTerm.subjects?.length > 0 && (
                            <div className="space-y-2">
                              <Label>Subjects Added ({newTerm.subjects.length})</Label>
                              <div className="space-y-2">
                                {newTerm.subjects.map((subject, index) => (
                                  <div key={index} className="flex items-center justify-between bg-muted p-2 rounded">
                                    <span className="text-sm">
                                      {subject?.subjectName} - {subject?.mark}%
                                    </span>
                                    <Button
                                      variant="ghost"
                                      size="sm"
                                      onClick={() => handleRemoveSubject(index)}
                                    >
                                      <X className="h-4 w-4" />
                                    </Button>
                                  </div>
                                ))}
                              </div>
                              <div className="bg-primary/10 p-2 rounded">
                                <span className="text-sm font-medium">
                                  Average: {calculateAverage(newTerm.subjects.map(s => s.mark))}%
                                </span>
                              </div>
                            </div>
                          )}

                          <div className="flex gap-2">
                            <Button
                              onClick={() => handleAddTerm(year?.id)}
                              disabled={addTermMutation.isPending}
                            >
                              {addTermMutation.isPending ? 'Adding...' : 'Add Term'}
                            </Button>
                            <Button
                              variant="outline"
                              onClick={() => {
                                setIsAddingTerm(null);
                                setNewTerm({ termNumber: '', subjects: [] });
                                setNewSubject({ subjectName: '', mark: '' });
                              }}
                            >
                              Cancel
                            </Button>
                          </div>
                        </CardContent>
                      </Card>
                    )}

                    {/* Terms Display */}
                    {year?.terms?.length === 0 ? (
                      <p className="text-sm text-muted-foreground text-center py-4">
                        No terms added yet
                      </p>
                    ) : (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {year?.terms?.map((term) => (
                          <Card key={term?.id}>
                            <CardHeader className="pb-3">
                              <div className="flex items-center justify-between">
                                <CardTitle className="text-base">
                                  Term {term?.termNumber || 'N/A'}
                                </CardTitle>
                                <Badge variant="secondary">
                                  Avg: {term?.averageMark || 0}%
                                </Badge>
                              </div>
                            </CardHeader>
                            <CardContent>
                              <div className="space-y-2">
                                {term?.subjects?.map((subject) => (
                                  <div key={subject?.id} className="flex justify-between text-sm">
                                    <span className="text-muted-foreground">
                                      {subject?.subjectName || 'N/A'}
                                    </span>
                                    <span className="font-medium">
                                      {subject?.mark || 0}%
                                    </span>
                                  </div>
                                ))}
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Followers Tab */}
        <TabsContent value="followers" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Providers Following You</CardTitle>
              <CardDescription>
                These providers are tracking your academic progress
              </CardDescription>
            </CardHeader>
            <CardContent>
              {followers?.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-muted-foreground">No providers are following you yet</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {followers?.map((follow) => (
                    <div key={follow?.followId} className="flex items-center justify-between p-4 border rounded-lg">
                      <div>
                        <p className="font-medium">{follow?.providerName || 'Unknown Provider'}</p>
                        <p className="text-sm text-muted-foreground">
                          Following since {follow?.followedAt ? formatRelativeTime(follow.followedAt) : 'N/A'}
                        </p>
                        {follow?.notes && (
                          <p className="text-sm text-muted-foreground mt-2">
                            <strong>Notes:</strong> {follow.notes}
                          </p>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

export default Profile;