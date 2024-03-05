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
      fontFamily: {
        sans: ['var(--font-sans)', ...fontFamily.sans],
      },
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
          DEFAULT: '#006874',
          foreground: 'hsl(var(--primary-foreground))',
          '0': '#000000',
          '5': '#041316',
          '10': '#0F1E20',
          '15': '#19282B',
          '20': '#243335',
          '25': '#2F3E41',
          '30': '#3A494C',
          '35': '#465558',
          '40': '#516164',
          '50': '#6A7A7D',
          '60': '#839496',
          '70': '#9EAEB1',
          '80': '#B9CACC',
          '90': '#D5E6E9',
          '95': '#E3F4F7',
          '98': '#EDFCFF',
          '99': '#F6FEFF',
          '100': '#FFFFFF',
        },
        secondary: {
          DEFAULT: '#4A6267',
          foreground: 'hsl(var(--secondary-foreground))',
          '0': '#000000',
          '5': '#0E1112',
          '10': '#191C1D',
          '15': '#232627',
          '20': '#2E3132',
          '25': '#393C3C',
          '30': '#444748',
          '35': '#505353',
          '40': '#5C5F5F',
          '50': '#757778',
          '60': '#8E9192',
          '70': '#A9ACAC',
          '80': '#C5C7C7',
          '90': '#E1E3E3',
          '95': '#EFF1F1',
          '98': '#F8FAFA',
          '99': '#FBFCFD',
          '100': '#FFFFFF',
        },
        tertiary: {
          DEFAULT: '#525E7D',
          '0': '#000000',
          '5': '#0F1115',
          '10': '#1A1B20',
          '15': '#24262B',
          '20': '#2F3035',
          '25': '#3A3B41',
          '30': '#45464C',
          '35': '#515258',
          '40': '#5D5E64',
          '50': '#76777D',
          '60': '#909096',
          '70': '#ABABB1',
          '80': '#C6C6CC',
          '90': '#E2E2E9',
          '95': '#F1F0F7',
          '98': '#FAF8FF',
          '99': '#FEFBFF',
          '100': '#FFFFFF',
        },
        neutral: {
          '0': '#000000',
          '5': '#111111',
          '10': '#1B1C1C',
          '15': '#262626',
          '20': '#303030',
          '25': '#3C3B3B',
          '30': '#474746',
          '35': '#535252',
          '40': '#5F5E5E',
          '50': '#787776',
          '60': '#929090',
          '70': '#ADABAA',
          '80': '#C8C6C5',
          '90': '#E4E2E1',
          '95': '#F3F0EF',
          '98': '#FCF9F8',
          '99': '#FFFBFB',
          '100': '#FFFFFF',
        },
        'neutral-variant': {
          '0': '#000000',
          '5': '#101111',
          '10': '#1B1C1C',
          '15': '#252626',
          '20': '#303031',
          '25': '#3B3B3C',
          '30': '#464747',
          '35': '#525253',
          '40': '#5E5E5E',
          '50': '#777777',
          '60': '#919190',
          '70': '#ABABAB',
          '80': '#C7C6C6',
          '90': '#E3E2E2',
          '95': '#F2F0F0',
          '98': '#FAF9F9',
          '99': '#FDFCFC',
          '100': '#FFFFFF',
        },
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
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
