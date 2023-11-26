/**
 * @file    HomePage.test.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   [TODO]
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

// !STARTERCONF You should delete this page

import { render } from '@testing-library/react';
import { Provider } from 'react-redux';

import { store } from '@/lib/redux/store';

import HomePage from '@/app/page';

describe('Homepage', () => {
  it('renders the simulation schema', () => {
    render(
      <Provider store={store}>
        <HomePage />
      </Provider>,
    );

    // Check if there is a button for simulation forward
    // There should be a button with aria-label="Forward"
    const buttonForward = document.querySelector(
      'button[aria-label="Step forward"]',
    );
    expect(buttonForward).toBeInTheDocument();
  });
});
