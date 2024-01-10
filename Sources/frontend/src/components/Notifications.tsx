/**
 * @file    Notifications.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Display notifications in the corner of the screen
 *
 * @date    19 September 2023, 22:00 (created)
 *
 * @section Licence
 * This file is part of the Superscalar simulator app
 *
 * Copyright (C) 2023  Michal Majer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        dismissAfter: 4000,
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
