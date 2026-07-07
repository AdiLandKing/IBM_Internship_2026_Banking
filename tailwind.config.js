/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['Cormorant Garamond', 'Georgia', 'serif'],
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        gold: '0 18px 45px rgba(189, 153, 82, 0.22)',
        vault: '0 24px 70px rgba(14, 14, 20, 0.18)',
      },
    },
  },
  plugins: [],
};
