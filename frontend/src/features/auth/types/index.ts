export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
}
