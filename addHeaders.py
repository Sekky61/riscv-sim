import os

def getHeader(fileName):
  return f"""/**
 * @file    {fileName}
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
 */"""

#for all files, recursively
for root, dirs, files in os.walk("."):
  print("Searching in: " + root)
  # for each file with extension .ts or .tsx
  for file in files:
    print("File: " + file)
    if file.endswith(".ts") or file.endswith(".tsx"):
      # open file
      with open(os.path.join(root, file), 'r+') as f:
        # read file
        content = f.read()
        # if file doesn't contain header
        if content.find("Michal Majer") == -1:
          # Create a description using ChatGPT
          
          # prepend header
          f.seek(0, 0)
          f.write(getHeader(file) + "\n\n" + content)
          f.close()
          print("Added header to: " + os.path.join(root, file))
        else:
          print("File already contains header: " + os.path.join(root, file))
