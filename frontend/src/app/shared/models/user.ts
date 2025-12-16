export interface User {
  id?: string;
  name?: string;
  email: string;
  password?: string;
  token?: string;
  favoriteMovie?: string; // Security question answer for password reset
}
