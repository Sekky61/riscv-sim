import { useEffect } from 'react';
import NotificationsSystem, {
  atalhoTheme,
  dismissNotification,
  setUpNotifications,
} from 'reapop';

import { useAppDispatch, useAppSelector } from '@/lib/redux/hooks';

/**
 * Display notifications in the bottom right corner of the screen
 */
const Notifications = () => {
  const dispatch = useAppDispatch();
  // 1. Retrieve the notifications to display.
  const notifications = useAppSelector((state) => state.notifications);

  // Run once on mount
  useEffect(() => {
    setUpNotifications({
      defaultProps: {
        position: 'top-right',
        dismissible: true,
        dismissAfter: 2000,
      },
    });
  }, []);

  return (
    <div>
      <NotificationsSystem
        // 2. Pass the notifications you want Reapop to display.
        notifications={notifications}
        // 3. Pass the function used to dismiss a notification.
        dismissNotification={(id) => dispatch(dismissNotification(id))}
        // 4. Pass a builtIn theme or a custom theme.
        theme={atalhoTheme}
      />
    </div>
  );
};

export default Notifications;
