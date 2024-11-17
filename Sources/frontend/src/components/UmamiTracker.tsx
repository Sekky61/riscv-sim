'use client';

import { EnvContextType, env } from '@/constant/envProvider';
import { useEffect, useState } from 'react';

/**
 * @file    UmamiTracker.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Umami tracker script
 *
 * @date    12 November 2024, 10:00 (created)
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

type UmamiTrackerProps = {
  envObject: EnvContextType; // from server component
};

export function UmamiTracker({ envObject }: UmamiTrackerProps) {
  const basePath = envObject.basePath;
  const [umamiId, setUmamiId] = useState<string | null>(null);

  useEffect(() => {
    const fetchUmamiId = async () => {
      try {
        const response = await fetch(`${basePath}/api/umamiId`);
        if (response.ok) {
          const { id } = await response.json();
          setUmamiId(id);
        } else {
          console.error('Failed to fetch Umami ID');
        }
      } catch (error) {
        console.error('Error fetching Umami ID:', error);
      }
    };

    fetchUmamiId();
  }, [basePath]);
  if (!umamiId) {
    return null;
  }
  return (
    <script
      async
      defer
      data-website-id={umamiId}
      src={`${basePath}/analytics/script.js`}
    />
  );
}
