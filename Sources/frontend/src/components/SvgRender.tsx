// Hook to render svg.js function to element

import { Svg } from '@svgdotjs/svg.js';
import { RefObject, useEffect } from 'react';

export function useSvgRender(element: RefObject<HTMLElement>, fn: () => Svg) {
  useEffect(() => {
    if (element.current) {
      const svg = fn();
      svg.addTo(element.current);
      return () => {
        svg.remove();
      };
    } else {
      console.warn('Element is null');
    }
  }, [element, fn]);
}
