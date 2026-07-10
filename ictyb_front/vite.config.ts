import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

export default defineConfig(({ command }) => ({
  plugins: [tailwindcss(), reactRouter()],
  resolve: {
    tsconfigPaths: true,
  },
  server: {
    port: 9000,
    host: "0.0.0.0",   // 외부 IP(100.1.221.29)에서 접속하려면 필수
  },
  ssr: {
    // 프로덕션 빌드할 때 의존성을 번들에 삽입
    //npm run dev 는 개발 테스트  
    //npm run build 배포시
    noExternal: command === "build" ? true : undefined,
  },
}));
