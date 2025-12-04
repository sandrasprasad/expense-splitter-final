import { render, screen, fireEvent } from '@testing-library/react';
import App from './App';

// Mock the API calls so tests don't fail on network
jest.mock('./api', () => ({
  get: jest.fn(() => Promise.resolve({ data: [] })),
  post: jest.fn(() => Promise.resolve({ data: {} })),
  interceptors: {
    request: { use: jest.fn() }
  }
}));

describe('FairShare Frontend Tests', () => {

  beforeEach(() => {
    localStorage.clear();
  });

  test('Renders Login Page by default', () => {
    render(<App />);
    const title = screen.getByText(/FairShare/i);
    expect(title).toBeInTheDocument();
    
    const loginBtn = screen.getByText(/Log In/i);
    expect(loginBtn).toBeInTheDocument();
  });

  test('Switch to Sign Up mode', () => {
    render(<App />);
    const toggleLink = screen.getByText(/New\? Sign up/i);
    fireEvent.click(toggleLink);
    
    const signUpBtn = screen.getByText(/Sign Up/i);
    expect(signUpBtn).toBeInTheDocument();
  });
});