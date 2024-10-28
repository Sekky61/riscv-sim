/**
 * @file    tailwind.config.ts
 *
 * @author  Michal Majer
 *          Faculty of Information Technology
 *          Brno University of Technology
 *          xmajer21@stud.fit.vutbr.cz
 *
 * @brief   Configuration for tailwindcss
 *
 * @date    10 November 2023, 16:00 (created)
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

import type { Config } from 'tailwindcss';
import { fontFamily } from 'tailwindcss/defaultTheme';

export default {
  content: ['./src/**/*.{js,jsx,ts,tsx}'],
  darkMode: ['selector'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(var(--border))',
        input: '#6F797A',
        ring: 'hsl(var(--ring))',
        foreground: 'hsl(var(--foreground))',
        destructive: {
          DEFAULT: 'hsl(var(--destructive))',
          foreground: 'hsl(var(--destructive-foreground))',
        },
        muted: {
          DEFAULT: 'hsl(var(--muted))',
          foreground: 'hsl(var(--muted-foreground))',
        },
        accent: {
          DEFAULT: 'hsl(var(--accent))',
          foreground: 'hsl(var(--accent-foreground))',
        },
        popover: {
          DEFAULT: 'hsl(var(--popover))',
          foreground: 'hsl(var(--popover-foreground))',
        },
        card: {
          DEFAULT: 'hsl(var(--card))',
          foreground: 'hsl(var(--card-foreground))',
        },
        important: '#ff7171',
        surfaceTint: '#006874',
        onPrimary: '#FFFFFF',
        primaryContainer: '#9EEFFD',
        onPrimaryContainer: '#001F24',
        onSecondary: '#FFFFFF',
        secondaryContainer: '#CDE7EC',
        onSecondaryContainer: '#051F23',
        onTertiary: '#FFFFFF',
        tertiaryContainer: '#D9E2FF',
        onTertiaryContainer: '#0E1B37',
        error: '#BA1A1A',
        onError: '#FFFFFF',
        errorContainer: '#FFDAD6',
        onErrorContainer: '#410002',
        background: '#F5FAFB',
        onBackground: '#171D1E',
        surface: '#F5FAFB',
        onSurface: '#171D1E',
        surfaceVariant: '#DBE4E6',
        onSurfaceVariant: '#3F484A',
        outline: '#6F797A',
        outlineVariant: '#BFC8CA',
        shadow: '#000000',
        scrim: '#000000',
        inverseSurface: '#2B3133',
        inverseOnSurface: '#ECF2F3',
        inversePrimary: '#81D3E0',
        primaryFixed: '#9EEFFD',
        onPrimaryFixed: '#001F24',
        primaryFixedDim: '#81D3E0',
        onPrimaryFixedVariant: '#004F57',
        secondaryFixed: '#CDE7EC',
        onSecondaryFixed: '#051F23',
        secondaryFixedDim: '#B1CBD0',
        onSecondaryFixedVariant: '#334B4F',
        tertiaryFixed: '#D9E2FF',
        onTertiaryFixed: '#0E1B37',
        tertiaryFixedDim: '#BAC6EA',
        onTertiaryFixedVariant: '#3A4664',
        surfaceDim: '#D5DBDC',
        surfaceBright: '#F5FAFB',
        surfaceContainerLowest: '#FFFFFF',
        surfaceContainerLow: '#EFF5F6',
        surfaceContainer: '#E9EFF0',
        surfaceContainerHigh: '#E3E9EA',
        surfaceContainerHighest: '#DEE3E5',
        dark: {
          primary: '#81D3E0',
          surfaceTint: '#81D3E0',
          onPrimary: '#00363D',
          primaryContainer: '#004F57',
          onPrimaryContainer: '#9EEFFD',
          secondary: '#B1CBD0',
          onSecondary: '#1C3438',
          secondaryContainer: '#334B4F',
          onSecondaryContainer: '#CDE7EC',
          tertiary: '#BAC6EA',
          onTertiary: '#24304D',
          tertiaryContainer: '#3A4664',
          onTertiaryContainer: '#D9E2FF',
          error: '#FFB4AB',
          onError: '#690005',
          errorContainer: '#93000A',
          onErrorContainer: '#FFDAD6',
          background: '#0E1415',
          onBackground: '#DEE3E5',
          surface: '#0E1415',
          onSurface: '#DEE3E5',
          surfaceVariant: '#3F484A',
          onSurfaceVariant: '#BFC8CA',
          outline: '#899294',
          outlineVariant: '#3F484A',
          shadow: '#000000',
          scrim: '#000000',
          inverseSurface: '#DEE3E5',
          inverseOnSurface: '#2B3133',
          inversePrimary: '#006874',
          primaryFixed: '#9EEFFD',
          onPrimaryFixed: '#001F24',
          primaryFixedDim: '#81D3E0',
          onPrimaryFixedVariant: '#004F57',
          secondaryFixed: '#CDE7EC',
          onSecondaryFixed: '#051F23',
          secondaryFixedDim: '#B1CBD0',
          onSecondaryFixedVariant: '#334B4F',
          tertiaryFixed: '#D9E2FF',
          onTertiaryFixed: '#0E1B37',
          tertiaryFixedDim: '#BAC6EA',
          onTertiaryFixedVariant: '#3A4664',
          surfaceDim: '#0E1415',
          surfaceBright: '#343A3B',
          surfaceContainerLowest: '#090F10',
          surfaceContainerLow: '#171D1E',
          surfaceContainer: '#1B2122',
          surfaceContainerHigh: '#252B2C',
          surfaceContainerHighest: '#303637',
        },
        primary: {
          DEFAULT: '#1B667D',
          '0': '#000000',
          '5': '#00131A',
          '10': '#001F28',
          '15': '#002A36',
          '20': '#003544',
          '25': '#004152',
          '30': '#004D61',
          '35': '#005A70',
          '40': '#1B667D',
          '50': '#3B7F97',
          '60': '#5799B2',
          '70': '#72B4CD',
          '80': '#8ED0E9',
          '90': '#B8EAFF',
          '95': '#DDF4FF',
          '98': '#F3FBFF',
          '99': '#F9FDFF',
          '100': '#FFFFFF',
        },
        secondary: {
          DEFAULT: '#536066',
          '0': '#000000',
          '5': '#061317',
          '10': '#101D22',
          '15': '#1B282D',
          '20': '#253238',
          '25': '#303D43',
          '30': '#3C494E',
          '35': '#47545A',
          '40': '#536066',
          '50': '#6C797F',
          '60': '#859399',
          '70': '#9FADB4',
          '80': '#BBC9CF',
          '90': '#D7E5EC',
          '95': '#E5F3FA',
          '98': '#F3FBFF',
          '99': '#F9FDFF',
          '100': '#FFFFFF',
        },
        tertiary: {
          DEFAULT: '#5C5C73',
          '0': '#000000',
          '5': '#0E0F22',
          '10': '#191A2D',
          '15': '#232438',
          '20': '#2E2F43',
          '25': '#393A4E',
          '30': '#45455A',
          '35': '#505166',
          '40': '#5C5C73',
          '50': '#75758C',
          '60': '#8F8FA7',
          '70': '#AAA9C2',
          '80': '#C5C4DE',
          '90': '#E1E0FB',
          '95': '#F1EFFF',
          '98': '#FCF8FF',
          '99': '#FFFBFF',
          '100': '#FFFFFF',
        },
        neutral: {
          '0': '#000000',
          '5': '#0F1112',
          '10': '#1A1C1D',
          '15': '#242627',
          '20': '#2F3132',
          '25': '#3A3C3D',
          '30': '#454748',
          '35': '#515353',
          '40': '#5D5E5F',
          '50': '#767778',
          '60': '#909192',
          '70': '#ABABAC',
          '80': '#C6C6C7',
          '90': '#E2E2E3',
          '95': '#F1F0F1',
          '98': '#FAF9FA',
          '99': '#FCFCFD',
          '100': '#FFFFFF',
        },
        'neutral-variant': {
          '0': '#000000',
          '5': '#0D1214',
          '10': '#171C1E',
          '15': '#222629',
          '20': '#2C3133',
          '25': '#373C3E',
          '30': '#43474A',
          '35': '#4E5356',
          '40': '#5A5F62',
          '50': '#73787A',
          '60': '#8D9194',
          '70': '#A7ACAF',
          '80': '#C3C7CA',
          '90': '#DFE3E6',
          '95': '#EDF1F4',
          '98': '#F6FAFD',
          '99': '#F9FDFF',
          '100': '#FFFFFF',
        },
      },
      keyframes: {
        flicker: {
          '0%, 19.999%, 22%, 62.999%, 64%, 64.999%, 70%, 100%': {
            opacity: '0.99',
            filter:
              'drop-shadow(0 0 1px rgba(252, 211, 77)) drop-shadow(0 0 15px rgba(245, 158, 11)) drop-shadow(0 0 1px rgba(252, 211, 77))',
          },
          '20%, 21.999%, 63%, 63.999%, 65%, 69.999%': {
            opacity: '0.4',
            filter: 'none',
          },
        },
        shimmer: {
          '0%': {
            backgroundPosition: '-700px 0',
          },
          '100%': {
            backgroundPosition: '700px 0',
          },
        },
        'accordion-down': {
          from: { height: '0' },
          to: { height: 'var(--radix-accordion-content-height)' },
        },
        'accordion-up': {
          from: { height: 'var(--radix-accordion-content-height)' },
          to: { height: '0' },
        },
      },
      animation: {
        flicker: 'flicker 3s linear infinite',
        shimmer: 'shimmer 1.3s linear infinite',
        'accordion-down': 'accordion-down 0.2s ease-out',
        'accordion-up': 'accordion-up 0.2s ease-out',
      },
      gridTemplateRows: {
        layout: 'auto 1fr auto',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
} satisfies Config;
