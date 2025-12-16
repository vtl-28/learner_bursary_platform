import { Link, useNavigate } from 'react-router-dom';
import { Bell, User, LogOut } from 'lucide-react';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Badge } from '@/components/ui/badge';
import useAuthStore from '@/lib/stores/authStore';
import { ROUTES } from '@/utils/constants';
import { useQuery } from '@tanstack/react-query';
import { notificationsAPI } from '@/api/notifications';

export default function ProviderHeader() {
  const navigate = useNavigate();
  const { user, clearAuth } = useAuthStore();

  // Fetch unread notification count
//   const { data: unreadCount } = useQuery({
//     queryKey: ['unreadNotifications'],
//     queryFn: async () => {
//       try {
//         const response = await notificationsAPI.getUnreadCount();
//         return response?.data || 0;
//       } catch (error) {
//         console.error('Error fetching notification count:', error);
//         return 0;
//     },
//     refetchInterval: 2000,
//     retry: false,
//     enabled: !!user,
//   });
    const unreadCount = 0;

  const handleLogout = () => {
    clearAuth();
    navigate(ROUTES.PROVIDER_LOGIN);
  };

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container flex h-16 items-center">
        {/* Logo */}
        <Link to={ROUTES.PROVIDER_DASHBOARD} className="mr-8 flex items-center space-x-2">
          <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
            BursaryHub
          </span>
          <Badge variant="secondary" className="ml-2">Provider</Badge>
        </Link>

        {/* Navigation Tabs */}
        <nav className="flex items-center space-x-6 text-sm font-medium flex-1">
          <Link
            to={ROUTES.PROVIDER_APPLICATIONS}
            className="transition-colors hover:text-foreground/80 text-foreground"
          >
            Applications
          </Link>
          <Link
            to={ROUTES.PROVIDER_FOLLOWING}
            className="transition-colors hover:text-foreground/80 text-foreground"
          >
            Following
          </Link>
          <Link
            to={ROUTES.PROVIDER_SEARCH}
            className="transition-colors hover:text-foreground/80 text-foreground"
          >
            Search Learners
          </Link>
          <Link
            to={ROUTES.PROVIDER_NOTIFICATIONS}
            className="transition-colors hover:text-foreground/80 text-foreground"
          >
            Notifications
          </Link>
        </nav>

        {/* Right side actions */}
        <div className="flex items-center space-x-4">
          {/* Notifications Bell */}
          <Button
            variant="ghost"
            size="icon"
            className="relative"
            onClick={() => navigate(ROUTES.PROVIDER_NOTIFICATIONS)}
          >
            <Bell className="h-5 w-5" />
            {unreadCount > 0 && (
              <Badge
                variant="destructive"
                className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs"
              >
                {unreadCount}
              </Badge>
            )}
          </Button>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon">
                <User className="h-5 w-5" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56">
              <DropdownMenuLabel>
                <div className="flex flex-col space-y-1">
                  <p className="text-sm font-medium leading-none">
                    {user?.organizationName}
                  </p>
                  <p className="text-xs leading-none text-muted-foreground">
                    {user?.email}
                  </p>
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={handleLogout}>
                <LogOut className="mr-2 h-4 w-4" />
                <span>Log out</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </header>
  );
}