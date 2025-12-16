import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { GraduationCap, Building2, ArrowRight } from 'lucide-react';
import { ROUTES } from '@/utils/constants';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-500 via-purple-500 to-pink-500">
      <div className="container mx-auto px-4 py-16">
        {/* Hero Section */}
        <div className="text-center text-white mb-16">
          <h1 className="text-6xl font-bold mb-4">BursaryHub</h1>
          <p className="text-xl mb-8">Connecting learners with life-changing opportunities</p>
        </div>

        {/* Cards Section */}
        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {/* Learner Card */}
          <Card className="hover:shadow-2xl transition-shadow">
            <CardHeader>
              <div className="flex items-center justify-center w-16 h-16 rounded-full bg-blue-100 mb-4 mx-auto">
                <GraduationCap className="w-8 h-8 text-blue-600" />
              </div>
              <CardTitle className="text-center text-2xl">I'm a Learner</CardTitle>
              <CardDescription className="text-center">
                Find and apply for bursaries, showcase your academic achievements
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">✓ Browse available bursaries</p>
                <p className="text-sm text-muted-foreground">✓ Track your applications</p>
                <p className="text-sm text-muted-foreground">✓ Upload academic results</p>
                <p className="text-sm text-muted-foreground">✓ Get discovered by providers</p>
              </div>
              <div className="flex flex-col gap-2 pt-4">
                <Button asChild size="lg">
                  <Link to={ROUTES.LEARNER_LOGIN}>
                    Sign In <ArrowRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
                <Button asChild variant="outline" size="lg">
                  <Link to={ROUTES.LEARNER_SIGNUP}>Create Account</Link>
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Provider Card */}
          <Card className="hover:shadow-2xl transition-shadow">
            <CardHeader>
              <div className="flex items-center justify-center w-16 h-16 rounded-full bg-purple-100 mb-4 mx-auto">
                <Building2 className="w-8 h-8 text-purple-600" />
              </div>
              <CardTitle className="text-center text-2xl">I'm a Provider</CardTitle>
              <CardDescription className="text-center">
                Discover talented learners and manage your bursary programs
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <p className="text-sm text-muted-foreground">✓ Search top-performing learners</p>
                <p className="text-sm text-muted-foreground">✓ Manage applications efficiently</p>
                <p className="text-sm text-muted-foreground">✓ Track learner progress</p>
                <p className="text-sm text-muted-foreground">✓ Make bursary offers</p>
              </div>
              <div className="flex flex-col gap-2 pt-4">
                <Button asChild size="lg" variant="secondary">
                  <Link to={ROUTES.PROVIDER_LOGIN}>
                    Provider Sign In <ArrowRight className="ml-2 h-4 w-4" />
                  </Link>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}