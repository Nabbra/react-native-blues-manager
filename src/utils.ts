import { EventHandler } from './types';

export const hasValue = <T>(o: T | null | undefined): o is T =>
  o !== null && o !== undefined;

export const isEmpty = (o: unknown): o is null | undefined => !hasValue(o);

export const tryCall = <T extends (...args: any[]) => any>(
  fn?: T,
  ...args: Parameters<T>
): ReturnType<T> | undefined => {
  if (typeof fn === 'function') {
    return fn(...args);
  }
  return undefined;
};

export const isObject = (o: unknown): o is Record<string, unknown> =>
  typeof o === 'object' && o !== null && !Array.isArray(o);

export const entries = (
  o: Record<string, EventHandler>
): Array<[string, EventHandler]> =>
  Object.entries(o) as Array<[string, EventHandler]>;
