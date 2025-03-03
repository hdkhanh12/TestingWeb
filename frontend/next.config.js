  /** @type {import('next').NextConfig} */
  const nextConfig = {
    reactStrictMode: true,
    eslint: {
      ignoreDuringBuilds: true, // Tắt ESLint khi build trên Vercel
    },
    async rewrites() {
      return [
        {
          source: '/api/:path*',
          destination: 'http://localhost:8080/api/:path*',
        },
      ];
    },
  };
  
  module.exports = nextConfig;