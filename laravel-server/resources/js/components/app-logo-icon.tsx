import type { SVGAttributes } from 'react';

export default function AppLogoIcon(
    props: SVGAttributes<SVGSVGElement>
) {
    return (
        <svg
            {...props}
            viewBox="0 0 64 64"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
        >
            {/* Monitor */}
            <rect
                x="10"
                y="12"
                width="44"
                height="30"
                rx="4"
                stroke="currentColor"
                strokeWidth="4"
            />

            {/* Screen pulse */}
            <path
                d="M20 27H28L32 21L36 33L40 27H46"
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
                strokeLinejoin="round"
            />

            {/* Stand */}
            <path
                d="M24 52H40"
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
            />

            <path
                d="M32 42V52"
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
            />
        </svg>
    );
}