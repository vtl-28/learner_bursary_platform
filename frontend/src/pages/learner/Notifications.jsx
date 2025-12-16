import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { Bell, BellOff, CheckCheck, Users, FileText, TrendingUp, Mail } from 'lucide-react';
import { notificationsAPI } from '@/api/notifications';
import { formatRelativeTime } from '@/utils/helpers';
import { NOTIFICATION_TYPES } from '@/utils/constants';

function Notifications() {
  const queryClient = useQueryClient();
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState('all');

  // Fetch all notifications
  const { data: notificationsData, isLoading, error } = useQuery({
    queryKey: ['myNotifications'],
    queryFn: async () => {
      try {
        return await notificationsAPI.getAll();
      } catch (error) {
        console.error('Error fetching notifications:', error);
        return { data: [] };
      }
    },
    refetchInterval: 2000, // Refetch every 30 seconds
  });

  const allNotifications = notificationsData?.data || [];

  // Filter notifications
  const unreadNotifications = allNotifications?.filter(n => !n?.isRead) || [];
  const readNotifications = allNotifications?.filter(n => n?.isRead) || [];

  // Mark as read mutation
  const markAsReadMutation = useMutation({
    mutationFn: (notificationId) => notificationsAPI.markAsRead(notificationId),
    onSuccess: () => {
      queryClient.invalidateQueries(['myNotifications']);
      queryClient.invalidateQueries(['unreadNotifications']);
      toast({
        title: "Marked as Read",
        description: "Notification has been marked as read.",
      });
    },
    onError: (error) => {
      toast({
        title: "Failed to Mark as Read",
        description: error?.message || "Please try again.",
        variant: "destructive",
      });
    },
  });

  const handleMarkAsRead = (notificationId) => {
    if (!notificationId) return;
    markAsReadMutation.mutate(notificationId);
  };

  const getNotificationIcon = (type) => {
    switch (type) {
      case NOTIFICATION_TYPES.NEW_FOLLOWER:
        return <Users className="h-5 w-5 text-blue-600" />;
      case NOTIFICATION_TYPES.RESULT_UPDATE:
        return <TrendingUp className="h-5 w-5 text-green-600" />;
      case NOTIFICATION_TYPES.NEW_OFFER:
        return <Mail className="h-5 w-5 text-purple-600" />;
      case NOTIFICATION_TYPES.STATUS_UPDATE:
        return <FileText className="h-5 w-5 text-yellow-600" />;
      default:
        return <Bell className="h-5 w-5 text-gray-600" />;
    }
  };

  const getNotificationColor = (type) => {
    switch (type) {
      case NOTIFICATION_TYPES.NEW_FOLLOWER:
        return 'bg-blue-50 border-blue-200';
      case NOTIFICATION_TYPES.RESULT_UPDATE:
        return 'bg-green-50 border-green-200';
      case NOTIFICATION_TYPES.NEW_OFFER:
        return 'bg-purple-50 border-purple-200';
      case NOTIFICATION_TYPES.STATUS_UPDATE:
        return 'bg-yellow-50 border-yellow-200';
      default:
        return 'bg-gray-50 border-gray-200';
    }
  };

  // Notification Card Component
  const NotificationCard = ({ notification }) => {
    const isUnread = !notification?.isRead;

    return (
      <Card
        className={`${isUnread ? 'border-l-4 border-l-primary' : ''} ${
          isUnread ? getNotificationColor(notification?.notificationType) : ''
        } hover:shadow-md transition-shadow`}
      >
        <CardContent className="p-4">
          <div className="flex items-start gap-4">
            {/* Icon */}
            <div className="flex-shrink-0 mt-1">
              {getNotificationIcon(notification?.notificationType)}
            </div>

            {/* Content */}
            <div className="flex-1 min-w-0">
              <div className="flex items-start justify-between gap-2 mb-1">
                <h4 className="font-semibold text-sm">
                  {notification?.title || 'Notification'}
                </h4>
                {isUnread && (
                  <Badge variant="default" className="flex-shrink-0">
                    New
                  </Badge>
                )}
              </div>

              <p className="text-sm text-muted-foreground mb-2">
                {notification?.message || 'No message'}
              </p>

              <div className="flex items-center justify-between">
                <span className="text-xs text-muted-foreground">
                  {notification?.createdAt ? formatRelativeTime(notification.createdAt) : 'N/A'}
                </span>

                {isUnread && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleMarkAsRead(notification?.id)}
                    disabled={markAsReadMutation.isPending}
                  >
                    <CheckCheck className="mr-1 h-3 w-3" />
                    Mark as read
                  </Button>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  };

  // Loading State
  if (isLoading) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Notifications</h1>
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <Skeleton key={i} className="h-24 w-full" />
          ))}
        </div>
      </div>
    );
  }

  // Error State
  if (error) {
    return (
      <div className="space-y-6">
        <h1 className="text-3xl font-bold">Notifications</h1>
        <Alert variant="destructive">
          <AlertDescription>
            Failed to load notifications. Please try refreshing the page.
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
          <h1 className="text-3xl font-bold">Notifications</h1>
          <p className="text-muted-foreground">
            Stay updated on your applications and followers
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="secondary" className="text-lg px-3 py-1">
            {unreadNotifications?.length || 0} Unread
          </Badge>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Total Notifications</CardDescription>
            <CardTitle className="text-2xl">
              {allNotifications?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Unread</CardDescription>
            <CardTitle className="text-2xl text-primary">
              {unreadNotifications?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
        <Card>
          <CardHeader className="pb-3">
            <CardDescription>Read</CardDescription>
            <CardTitle className="text-2xl text-muted-foreground">
              {readNotifications?.length || 0}
            </CardTitle>
          </CardHeader>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="all">
            All ({allNotifications?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="unread">
            Unread ({unreadNotifications?.length || 0})
          </TabsTrigger>
          <TabsTrigger value="read">
            Read ({readNotifications?.length || 0})
          </TabsTrigger>
        </TabsList>

        {/* All Notifications */}
        <TabsContent value="all" className="mt-6 space-y-4">
          {allNotifications?.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Bell className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-2">No notifications yet</p>
                <p className="text-sm text-muted-foreground text-center">
                  You'll receive notifications when providers follow you or update your applications
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {allNotifications?.map((notification) => (
                <NotificationCard key={notification?.id} notification={notification} />
              ))}
            </div>
          )}
        </TabsContent>

        {/* Unread Notifications */}
        <TabsContent value="unread" className="mt-6 space-y-4">
          {unreadNotifications?.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <CheckCheck className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-2">All caught up!</p>
                <p className="text-sm text-muted-foreground">
                  You have no unread notifications
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {unreadNotifications?.map((notification) => (
                <NotificationCard key={notification?.id} notification={notification} />
              ))}
            </div>
          )}
        </TabsContent>

        {/* Read Notifications */}
        <TabsContent value="read" className="mt-6 space-y-4">
          {readNotifications?.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <BellOff className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground mb-2">No read notifications</p>
                <p className="text-sm text-muted-foreground">
                  Notifications you've read will appear here
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="space-y-4">
              {readNotifications?.map((notification) => (
                <NotificationCard key={notification?.id} notification={notification} />
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}

export default Notifications;