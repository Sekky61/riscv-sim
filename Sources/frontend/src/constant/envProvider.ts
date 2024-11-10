'use server';

import {
  simApiExternalPrefix,
  simApiInternalPrefix,
  isLocal,
  isProd,
  showLogger,
} from './env';

function envObject() {
  return {
    simApiExternalPrefix,
    simApiInternalPrefix,
    isLocal,
    isProd,
    showLogger,
  };
}

export type EnvContextType = ReturnType<typeof envObject>;

/**
 * Server side function to pull env to client at runtime
 */
export const env = async () => {
  return envObject();
};


