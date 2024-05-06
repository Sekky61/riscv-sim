/**
 * @file    Navbar.test.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Test for the Navbar component
 *
 * @date    07 April 2024, 22:00 (created)
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

import { render, screen } from '@testing-library/react';
import {Block} from '../simulation/Block';

describe('Block', () => {
  it('renders the simulation link', async () => {
    render(
      <Block title='Test block'>
        <div>Test content</div>
      </Block>,
    );

    // contains the title
    expect(screen.getByText('Test block')).not.toBeNull();
    // contains the content
    expect(screen.getByText('Test content')).not.toBeNull();
  });
});
