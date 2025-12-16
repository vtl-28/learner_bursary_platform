import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, Filter, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Skeleton } from '@/components/ui/skeleton';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Label } from '@/components/ui/label';
import { bursariesAPI } from '@/api/bursaries';
import { applicationsAPI } from '@/api/applications';
import { formatCurrency, formatDate } from '@/utils/helpers';
import { useToast } from '@/hooks/use-toast';

function BursariesPage() {
  const { toast } = useToast();
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [selectedBursary, setSelectedBursary] = useState(null);
  const [isApplying, setIsApplying] = useState(false);

  // Filters
  const [filters, setFilters] = useState({
    minAmount: '',
    maxAmount: '',
    providerType: '',
    location: '',
  });

  // Fetch bursaries
    // Fetch bursaries
    const { data: bursariesData, isLoading, refetch } = useQuery({
      queryKey: ['bursaries', filters],
      queryFn: async () => {
        try {
          // If filters are applied, use search endpoint
          if (Object.values(filters).some(v => v !== '')) {
            const params = {};
            if (filters.minAmount) params.minAmount = filters.minAmount;
            if (filters.maxAmount) params.maxAmount = filters.maxAmount;
            if (filters.providerType) params.providerType = filters.providerType;
            if (filters.location) params.location = filters.location;

            return await bursariesAPI.search(params);
          }
          // Otherwise get all available
          return await bursariesAPI.getAll();
        } catch (error) {
          console.error('Error fetching bursaries:', error);
          return { data: [] };
        }
      },
    });

  const bursaries = bursariesData?.data || [];

  // Filter by search term (client-side)
  const filteredBursaries = bursaries.filter(bursary => {
    if (!searchTerm) return true;
    const searchLower = searchTerm.toLowerCase();

    const title = bursary?.title?.toLowerCase() || '';
    const orgName = bursary?.provider?.organizationName?.toLowerCase() || '';
    const description = bursary?.description?.toLowerCase() || '';

     return (
          title.includes(searchLower) ||
          orgName.includes(searchLower) ||
          description.includes(searchLower)
        );
  });

  const handleApply = async (bursaryId) => {
      if (!bursaryId) {
          toast({
                    title: "Invalid bursary ID",
                    description: "Bursary does not exist.",
                    variant: "destructive",
                  });

            return;
          }
    try {
      setIsApplying(true);

      // Check if already applied
      const checkResponse = await applicationsAPI.checkIfApplied(bursaryId);

      if (checkResponse?.data?.hasApplied) {
        toast({
          title: "Already Applied",
          description: "You have already applied to this bursary.",
          variant: "destructive",
        });
        setSelectedBursary(null);
        return;
      }

      // Apply
      await applicationsAPI.apply(bursaryId);

      toast({
        title: "Application Submitted!",
        description: "Your application has been submitted successfully.",
      });

      setSelectedBursary(null);
    } catch (error) {
      toast({
        title: "Application Failed",
        description: error.message || "Failed to submit application. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsApplying(false);
    }
  };

  const clearFilters = () => {
    setFilters({
      minAmount: '',
      maxAmount: '',
      providerType: '',
      location: '',
    });
  };

  const hasActiveFilters = Object.values(filters).some(v => v !== '');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Available Bursaries</h1>
          <p className="text-muted-foreground">
            Browse and apply for bursaries that match your profile
          </p>
        </div>
        <Button
          variant={showFilters ? "default" : "outline"}
          onClick={() => setShowFilters(!showFilters)}
        >
          <Filter className="mr-2 h-4 w-4" />
          Filters
          {hasActiveFilters && (
            <Badge variant="secondary" className="ml-2">
              Active
            </Badge>
          )}
        </Button>
      </div>

      {/* Search Bar */}
      <div className="relative">
        <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Search bursaries by title, provider, or description..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="pl-10"
        />
      </div>

      {/* Filters Panel */}
      {showFilters && (
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Filters</CardTitle>
              {hasActiveFilters && (
                <Button variant="ghost" size="sm" onClick={clearFilters}>
                  <X className="mr-2 h-4 w-4" />
                  Clear All
                </Button>
              )}
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="space-y-2">
                <Label>Min Amount (ZAR)</Label>
                <Input
                  type="number"
                  placeholder="50000"
                  value={filters.minAmount}
                  onChange={(e) => setFilters({ ...filters, minAmount: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label>Max Amount (ZAR)</Label>
                <Input
                  type="number"
                  placeholder="200000"
                  value={filters.maxAmount}
                  onChange={(e) => setFilters({ ...filters, maxAmount: e.target.value })}
                />
              </div>
              <div className="space-y-2">
                <Label>Provider Type</Label>
                <Select
                  value={filters.providerType}
                  onValueChange={(value) => setFilters({ ...filters, providerType: value })}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="All Types" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">All Types</SelectItem>
                    <SelectItem value="Bank">Bank</SelectItem>
                    <SelectItem value="NGO">NGO</SelectItem>
                    <SelectItem value="Corporate">Corporate</SelectItem>
                    <SelectItem value="Government">Government</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label>Location</Label>
                <Input
                  placeholder="Gauteng"
                  value={filters.location}
                  onChange={(e) => setFilters({ ...filters, location: e.target.value })}
                />
              </div>
            </div>
            <div className="mt-4 flex justify-end">
              <Button onClick={() => refetch()}>Apply Filters</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Results Count */}
      <div className="text-sm text-muted-foreground">
        Showing {filteredBursaries?.length} bursaries
      </div>

      {/* Bursaries Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
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
      ) : filteredBursaries.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <p className="text-muted-foreground mb-4">No bursaries found matching your criteria.</p>
            {hasActiveFilters && (
              <Button variant="outline" onClick={clearFilters}>
                Clear Filters
              </Button>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredBursaries.map((bursary) => (
            <Card key={bursary.id} className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="line-clamp-2">{bursary?.title}</CardTitle>
                    <CardDescription className="mt-1">
                      {bursary?.provider?.organizationName || 'Unknown Provider'}
                    </CardDescription>
                  </div>
                  <Badge variant="secondary">{bursary?.provider?.organizationType || 'N/A'}</Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <p className="text-sm text-muted-foreground line-clamp-3">
                  {bursary?.description || 'No description available'}
                </p>
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Amount:</span>
                    <span className="font-semibold text-green-600">
                      {bursary?.amount ? formatCurrency(bursary.amount) : 'N/A'}
                    </span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Deadline:</span>
                    <span className="font-medium">  {bursary?.applicationDeadline ? formatDate(bursary.applicationDeadline) : 'N/A'}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Location:</span>
                    <span>{bursary?.provider?.location || 'N/A'}</span>
                  </div>
                </div>
              </CardContent>
              <CardFooter>
                <Button
                  className="w-full"
                  onClick={() => setSelectedBursary(bursary)}
                >
                  View Details
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}

      {/* Bursary Detail Dialog */}
      <Dialog open={!!selectedBursary} onOpenChange={() => setSelectedBursary(null)}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          {selectedBursary && (
            <>
              <DialogHeader>
                <DialogTitle className="text-2xl">{selectedBursary?.title || 'Untitled Bursary'}</DialogTitle>
                <DialogDescription className="text-base">
                   {selectedBursary?.provider?.organizationName || 'Unknown Provider'} • {selectedBursary?.provider?.organizationType || 'N/A'}
                </DialogDescription>
              </DialogHeader>

              <div className="space-y-6 py-4">
                {/* Key Info */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">Amount</p>
                    <p className="text-2xl font-bold text-green-600">
                      {selectedBursary?.amount ? formatCurrency(selectedBursary.amount) : 'N/A'}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-sm text-muted-foreground">Application Deadline</p>
                    <p className="text-lg font-semibold">
                      {selectedBursary?.applicationDeadline ? formatDate(selectedBursary.applicationDeadline) : 'N/A'}
                    </p>
                  </div>
                </div>

                {/* Description */}
                <div className="space-y-2">
                  <h3 className="font-semibold">About This Bursary</h3>
                  <p className="text-sm text-muted-foreground">{selectedBursary?.description || 'No description available'}</p>
                </div>

                {/* Provider Info */}
                <div className="space-y-2">
                  <h3 className="font-semibold">Provider Information</h3>
                  <div className="grid grid-cols-2 gap-2 text-sm">
                    <div>
                      <span className="text-muted-foreground">Organization:</span>
                      <p className="font-medium"> {selectedBursary?.provider?.organizationName || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Type:</span>
                      <p className="font-medium">{selectedBursary?.provider?.organizationType || 'N/A'}</p>
                    </div>
                    <div>
                      <span className="text-muted-foreground">Location:</span>
                      <p className="font-medium">{selectedBursary?.provider?.location || 'N/A'}</p>
                    </div>
                  </div>
                </div>

                {/* Criteria (if available) */}
                {selectedBursary?.criteria && (
                  <div className="space-y-2">
                    <h3 className="font-semibold">Eligibility Criteria</h3>
                    <div className="bg-muted p-4 rounded-lg">
                      <pre className="text-sm whitespace-pre-wrap">
                        {(() => {
                                                  try {
                                                    return JSON.stringify(JSON.parse(selectedBursary.criteria), null, 2);
                                                  } catch {
                                                    return selectedBursary.criteria;
                                                  }
                                                })()}
                      </pre>
                    </div>
                  </div>
                )}
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setSelectedBursary(null)}>
                  Close
                </Button>
                <Button
                  onClick={() => handleApply(selectedBursary.id)}
                  disabled={isApplying}
                >
                  {isApplying ? "Applying..." : "Apply Now"}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default BursariesPage;