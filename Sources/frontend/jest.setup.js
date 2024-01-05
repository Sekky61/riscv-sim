import '@testing-library/jest-dom';

// Allow router mocks.
// eslint-disable-next-line no-undef
jest.mock('next/router', () => require('next-router-mock'));

/**
 * Importing next during test should apply polyfills, but doesn't seem to work.
 *
 */
require('next');
