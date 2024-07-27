/**
 * @file    ExternalLink.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   External link component
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

export interface CenterLayoutProps {
  href: string;
  openInNewTab?: boolean;
  children: React.ReactNode;
}

/**
 * Link with an arrow
 */
export function ExternalLink({
  href,
  children,
  openInNewTab = false,
}: CenterLayoutProps) {
  return (
    <a
      href={href}
      target={openInNewTab ? '_blank' : '_self'}
      className='inline-flex items-center font-medium text-blue-600 hover:cursor-pointer hover:underline'
    >
      {children}
      <svg
        className='ml-2 h-4 w-4'
        aria-hidden='true'
        xmlns='http://www.w3.org/2000/svg'
        fill='none'
        viewBox='0 0 14 10'
      >
        <path
          stroke='currentColor'
          strokeLinecap='round'
          strokeLinejoin='round'
          strokeWidth='2'
          d='M1 5h12m0 0L9 1m4 4L9 9'
        />
      </svg>
    </a>
  );
}
