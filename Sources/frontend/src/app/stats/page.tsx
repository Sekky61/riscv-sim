/**
 * @file    page.tsx
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   The stats page
 *
 * @date    01 December 2023, 20:00 (created)
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

import { CardDescription, CardHeader, CardContent, Card } from "@/components/base/ui/card"
import { TableHead, TableRow, TableHeader, TableCell, TableBody, Table } from "@/components/base/ui/table"

export default function Component() {
  return (
    <div className="">
      <div className="flex flex-col">
        <main className="flex flex-1 flex-col gap-4 p-4 md:gap-8 md:p-6">
          <div className="flex items-center gap-4">
            <h1 className="font-semibold text-lg md:text-xl">Simulation Dashboard</h1>
          </div>
          <div className="grid gap-6">
            <div className="grid md:grid-cols-3 gap-6">
              <Card>
                <CardHeader>
                  <CardDescription>Stalls</CardDescription>
                </CardHeader>
                <CardContent>
                  <span className="text-8xl">0</span>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardDescription>Clocks</CardDescription>
                </CardHeader>
                <CardContent>
                <span className="text-8xl">95</span>
                </CardContent>
              </Card>
              <Card>
                <CardHeader>
                  <CardDescription>Branch Prediction</CardDescription>
                </CardHeader>
                <CardContent>
                <span className="text-8xl">94%</span>
                </CardContent>
              </Card>
            </div>
            <h2 className="font-semibold text-lg md:text-xl">Detailed Statistics</h2>
            <div className="border shadow-sm rounded-lg">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="w-[100px]">Category</TableHead>
                    <TableHead>Value</TableHead>
                    <TableHead>Percentage</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow>
                    <TableCell className="font-medium">Stalls</TableCell>
                    <TableCell>350</TableCell>
                    <TableCell className="text-right">35%</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="font-medium">Clocks</TableCell>
                    <TableCell>450</TableCell>
                    <TableCell className="text-right">45%</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell className="font-medium">Branch Prediction</TableCell>
                    <TableCell>200</TableCell>
                    <TableCell className="text-right">20%</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
