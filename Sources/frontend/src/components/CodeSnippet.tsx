/**
 * @file    CodeSnippet.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Read-only code snippet. For use in the help page.
 *
 * @date    27 February 2024, 09:00 (created)
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

type CodeSnippetProps = {
  code?: string;
  language: string;
  children?: React.ReactNode;
};

/**
 * Read-only code snippet. For use in the help page.
 * TODO: ability to highlight a portion of the code
 */
export function CodeSnippet({ code, language, children }: CodeSnippetProps) {
  const toDisplay = children || code;
  return (
    <pre className='border p-4 rounded-md m-4'>
      <code className={`language-${language}`}>{toDisplay}</code>
    </pre>
  );
}
