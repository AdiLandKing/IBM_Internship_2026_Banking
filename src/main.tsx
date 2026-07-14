import React from 'react';
import ReactDOM from 'react-dom/client';
import { CardElement, Elements, useElements, useStripe } from '@stripe/react-stripe-js';
import { loadStripe, type StripeCardElementOptions } from '@stripe/stripe-js';
import { Activity, ArrowLeft, Calendar, Check, ChartPie, Copy, CreditCard, Eye, EyeOff, KeyRound, Landmark, LockKeyhole, Mail, Moon, Pencil, Plus, Search, Settings, ShieldCheck, Sun, Unlock, UserPlus, UserRound, Users, Wallet, X, Zap } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import './styles.css';

const stats = [
  { value: '$2.4T', label: 'Assets Under Stewardship' },
  { value: '12,000+', label: 'Distinguished Families' },
  { value: 'Since 1847', label: 'Heritage of Excellence' },
  { value: '180', label: 'Countries Served' },
];

const adminMetrics = [
  { label: 'Active Clients', value: '12,084', detail: '+42 this month', icon: Users },
  { label: 'Transfers Pending', value: '18', detail: '$2.8M under review', icon: Activity },
  { label: 'KYC Reviews', value: '7', detail: '3 high priority', icon: ShieldCheck },
  { label: 'Managed Assets', value: '$2.4T', detail: 'Across 180 countries', icon: Landmark },
];

const clientQueue = [
  { name: 'Hamilton Family Office', request: 'International transfer approval', priority: 'High', time: '12 min ago' },
  { name: 'Mori Capital Trust', request: 'New portfolio mandate', priority: 'Medium', time: '41 min ago' },
  { name: 'Ainsley Holdings', request: 'Profile verification', priority: 'Low', time: '2 hr ago' },
  { name: 'Zurich Custody Account', request: 'Wire beneficiary review', priority: 'High', time: '3 hr ago' },
];

type AdminMenuId = 'pending' | 'users' | 'logs' | 'access';

const adminMenus: { id: AdminMenuId; label: string; description: string; icon: LucideIcon }[] = [
  { id: 'pending', label: 'Pending Transactions', description: 'Approve or reject queued transfers', icon: Wallet },
  { id: 'users', label: 'Search Users', description: 'Find client profiles and roles', icon: Users },
  { id: 'logs', label: 'Transfer Logs', description: 'Audit completed transfer activity', icon: Activity },
  { id: 'access', label: 'User Access', description: 'Create users or grant admin access', icon: ShieldCheck },
];

const accessRequests = [
  { name: 'Iris Kovan', email: 'iris.kovan@safebank.com', request: 'Admin approval', submitted: 'Today' },
  { name: 'Victor Lane', email: 'victor@example.com', request: 'Client account creation', submitted: 'Yesterday' },
  { name: 'Maya Chen', email: 'maya.chen@safebank.com', request: 'Advisor account creation', submitted: 'Jul 01, 2026' },
];

type ClientAccount = {
  id: string;
  name: string;
  type: string;
  iban: string;
  balance: number;
  currency: string;
  status: AccountStatus;
  opened: string;
  branch: string;
};

type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'BLOCKED';

type AccountResponse = {
  iban: string;
  name: string;
  balance: number | string;
  currency: string;
  status: AccountStatus;
  createdAt?: string | null;
};

type RecipientAccountResponse = {
  iban: string;
  currency: string;
};

type ApiPage<T> = {
  content: T[];
  totalElements?: number;
  totalPages?: number;
};

type AdminUserResponse = {
  email: string;
  firstName: string;
  lastName: string;
  role: 'USER' | 'ADMIN';
  accountCount: number;
  active: boolean;
  accounts: AccountResponse[];
};

type AdminTransactionResponse = {
  transactionId: number;
  sourceIban: string;
  destinationIban: string;
  amount: number | string;
  sourceCurrency: string;
  creditedAmount: number | string;
  destinationCurrency: string;
  reason: string;
  timeStamp: string;
  status: string;
  exchangeRateUsed: number | string;
};

type AccountCurrency = 'BGN' | 'EUR' | 'USD' | 'GBP';

const ACCOUNT_CURRENCIES: AccountCurrency[] = ['BGN', 'EUR', 'USD', 'GBP'];

type PortfolioHolding = {
  name: string;
  category: string;
  value: string;
  allocation: string;
  allocationWidth: string;
  status: string;
};

type AccountPortfolioHolding = PortfolioHolding & {
  account: ClientAccount;
};

type TransferDraft = {
  sourceAccountIban: string;
  destinationAccountIban: string;
  amount: string;
  currency: string;
  reason: string;
};

type TransferValidationErrors = Partial<Record<keyof TransferDraft, string>>;

type TransactionStatus = 'PENDING' | 'COMPLETED' | 'FAILED';

type TransactionHistoryResponse = {
  transactionId: number;
  sourceIban: string;
  destinationIban: string;
  amount: number | string;
  creditedAmount: number | string | null;
  sourceCurrency: string | null;
  destinationCurrency: string | null;
  reason: string;
  status: TransactionStatus;
  timestamp: string;
};

type TransactionPageResponse = {
  content: TransactionHistoryResponse[];
};

type TransferResponse = {
  transactionId: number;
  status: TransactionStatus;
};

type PaymentIntentResponse = {
  clientSecret: string;
};

class ApiRequestError extends Error {
  readonly fieldErrors: Record<string, string>;

  constructor(message: string, fieldErrors: Record<string, string> = {}) {
    super(message);
    this.name = 'ApiRequestError';
    this.fieldErrors = fieldErrors;
  }
}

function getPageFromPath(pathname: string): PageMode {
  if (pathname === '/admin') return 'admin';
  if (pathname === '/accounts') return 'accounts';
  if (pathname === '/profile') return 'profile';
  if (pathname === '/portfolio') return 'portfolio';
  if (pathname === '/transactions') return 'transactions';
  return 'home';
}

function getPathFromPage(page: PageMode) {
  if (page === 'admin') return '/admin';
  if (page === 'accounts') return '/accounts';
  if (page === 'profile') return '/profile';
  if (page === 'portfolio') return '/portfolio';
  if (page === 'transactions') return '/transactions';
  return '/';
}

function Logo({ onClick }: { onClick?: () => void }) {
  return (
    <a
      href="#"
      onClick={(event) => {
        if (!onClick) return;
        event.preventDefault();
        onClick();
      }}
      className="flex items-center gap-3"
      aria-label="SAFE Bank home"
    >
      <span className="grid h-8 w-8 place-items-center rounded-full border-2 border-[rgb(var(--gold-soft))] bg-[rgb(var(--gold))] shadow-[inset_0_0_0_5px_rgba(255,255,255,0.24)]">
        <span className="h-3.5 w-3.5 rounded-full bg-[rgb(var(--logo-core))]" />
      </span>
      <span className="font-display text-[1.3rem] font-bold tracking-[0.02em] text-[rgb(var(--text-strong))]">
        SAFE Bank
      </span>
    </a>
  );
}

function Eyebrow({ children }: { children: React.ReactNode }) {
  return (
    <div className="mx-auto flex items-center justify-center gap-4 text-center text-[0.72rem] font-bold uppercase tracking-[0.36em] text-[rgb(var(--gold))]">
      <span className="h-px w-6 bg-[rgb(var(--gold))]/45" />
      <span>{children}</span>
      <span className="h-px w-6 bg-[rgb(var(--gold))]/45" />
    </div>
  );
}

type AuthMode = 'login' | 'register';
type PageMode = 'home' | 'accounts' | 'profile' | 'transactions' | 'portfolio' | 'admin';

const CONFIGURED_API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim();
const API_BASE_URL = typeof window !== 'undefined' && window.location.port !== '5173'
  ? window.location.origin
  : (CONFIGURED_API_BASE_URL || 'http://127.0.0.1:8080').replace(/\/$/, '');
const STRIPE_PUBLISHABLE_KEY = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY?.trim() ?? '';
const stripePromise = STRIPE_PUBLISHABLE_KEY ? loadStripe(STRIPE_PUBLISHABLE_KEY) : null;
const AUTH_STORAGE_KEY = 'safe-bank-auth-session';

type UserProfile = {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string | null;
  role: 'USER' | 'ADMIN';
  createdAt: string;
};

type AuthSession = {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  user: UserProfile;
};

type AuthenticationResult = AuthSession & {
  oneTimeEPin: string | null;
};

type ApiErrorResponse = {
  message?: string;
  fieldErrors?: Record<string, string>;
};

type AuthFieldErrors = Partial<Record<'email' | 'firstName' | 'lastName' | 'dateOfBirth' | 'ePin' | 'password' | 'confirmPassword', string>>;

const AUTH_RULE_MESSAGE = 'Password must be at least 10 characters and include 1 uppercase letter, 1 number, and 1 special character.';
const REGISTRATION_AUTH_PATTERN = String.raw`^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$`;
const EPIN_LENGTH = 6;
const EPIN_PATTERN = new RegExp(String.raw`^\d{${EPIN_LENGTH}}$`);
const EPIN_INPUT_PATTERN = `[0-9]{${EPIN_LENGTH}}`;
const EPIN_VALIDATION_MESSAGE = `E-PIN must contain exactly ${EPIN_LENGTH} digits.`;
const AUTH_INPUT_AUTOCOMPLETE = {
  loginEntry: 'current-password',
  newEntry: 'new-password',
} as const;
const AUTH_FIELD_IDS = {
  entryRequirements: 'password-requirements',
  entryError: 'password-error',
  confirmEntryError: 'confirm-password-error',
} as const;
const AUTH_FIELD_PLACEHOLDERS = {
  loginEntry: 'Enter your password',
  newEntry: 'Create a password',
  confirmEntry: 'Re-enter your password',
} as const;

function hasInteger(value: string) {
  return /\d/.test(value);
}

function sanitizeEPin(value: string) {
  return value.replace(/\D/g, '').slice(0, EPIN_LENGTH);
}

function isValidRegistrationPassword(password: string) {
  return password.length >= 10
    && /[A-Z]/.test(password)
    && /\d/.test(password)
    && /[^A-Za-z0-9]/.test(password);
}

function isValidEmail(value: string) {
  if (!value || value.length > 320 || value.includes(' ')) return false;

  const emailParts = value.split('@');
  if (emailParts.length !== 2) return false;

  const [localPart, domain] = emailParts;
  if (!localPart || !domain?.includes('.')) return false;

  return domain.split('.').every((part) => part.length > 0);
}

function isValidDateOfBirth(value: string) {
  if (!value) return false;

  const selectedDate = new Date(`${value}T00:00:00`);
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return !Number.isNaN(selectedDate.getTime()) && selectedDate < today;
}

function getLatestValidBirthDate() {
  const latestDate = new Date();
  latestDate.setDate(latestDate.getDate() - 1);
  return latestDate.toISOString().slice(0, 10);
}

function getFormString(formData: FormData, key: string, trim = false) {
  const value = formData.get(key);
  if (typeof value !== 'string') return '';

  return trim ? value.trim() : value;
}

function getRegistrationValidationErrors(formData: FormData): AuthFieldErrors {
  const email = getFormString(formData, 'email', true);
  const firstName = getFormString(formData, 'firstName', true);
  const lastName = getFormString(formData, 'lastName', true);
  const password = getFormString(formData, 'password');
  const confirmPassword = getFormString(formData, 'confirmPassword');
  const dateOfBirth = getFormString(formData, 'dateOfBirth');
  const ePin = getFormString(formData, 'ePin', true);
  const validationErrors: AuthFieldErrors = {};

  if (!email) {
    validationErrors.email = 'Email address is required.';
  } else if (!isValidEmail(email)) {
    validationErrors.email = 'Enter a valid email address.';
  }

  if (!firstName) {
    validationErrors.firstName = 'First name is required.';
  } else if (hasInteger(firstName)) {
    validationErrors.firstName = 'First name cannot contain numbers.';
  }

  if (!lastName) {
    validationErrors.lastName = 'Last name is required.';
  } else if (hasInteger(lastName)) {
    validationErrors.lastName = 'Last name cannot contain numbers.';
  }

  if (!isValidDateOfBirth(dateOfBirth)) {
    validationErrors.dateOfBirth = 'Enter a valid date of birth.';
  }

  if (ePin && !EPIN_PATTERN.test(ePin)) {
    validationErrors.ePin = EPIN_VALIDATION_MESSAGE;
  }

  if (!isValidRegistrationPassword(password)) {
    validationErrors.password = AUTH_RULE_MESSAGE;
  }

  if (!confirmPassword) {
    validationErrors.confirmPassword = 'Confirm your password.';
  } else if (password !== confirmPassword) {
    validationErrors.confirmPassword = 'Passwords must match.';
  }

  return validationErrors;
}

async function authenticate(mode: AuthMode, payload: Record<string, string>): Promise<AuthenticationResult> {
  const response = await fetch(`${API_BASE_URL}/api/auth/${mode === 'login' ? 'login' : 'register'}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    let apiError: ApiErrorResponse | null = null;

    try {
      apiError = await response.json();
    } catch {
      apiError = null;
    }

    const fieldError = apiError?.fieldErrors ? Object.values(apiError.fieldErrors)[0] : undefined;
    throw new Error(fieldError ?? apiError?.message ?? 'Authentication failed. Please try again.');
  }

  return response.json();
}

async function getApiErrorMessage(response: Response, fallback: string) {
  try {
    const apiError = await response.json() as ApiErrorResponse;
    const fieldError = apiError.fieldErrors ? Object.values(apiError.fieldErrors)[0] : undefined;
    return fieldError ?? apiError.message ?? fallback;
  } catch {
    return fallback;
  }
}

function getAuthorizationHeader(authSession: AuthSession) {
  return `${authSession.tokenType} ${authSession.accessToken}`;
}

async function fetchEPinStatus(authSession: AuthSession): Promise<boolean> {
  const response = await fetch(`${API_BASE_URL}/api/users/e-pin/status`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to load E-PIN status.'));
  }

  const result = await response.json() as { set: boolean };
  return result.set;
}

async function saveEPin(
  authSession: AuthSession,
  currentPassword: string,
  currentEPin: string | null,
  newEPin: string,
): Promise<void> {
  const isChange = currentEPin !== null;
  const response = await fetch(`${API_BASE_URL}/api/users/e-pin`, {
    method: isChange ? 'PUT' : 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(isChange
      ? { currentPassword, currentEPin, newEPin }
      : { currentPassword, newEPin }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(
      response,
      isChange ? 'Unable to change your E-PIN.' : 'Unable to set your E-PIN.',
    ));
  }
}

async function verifyEPin(authSession: AuthSession, ePin: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/users/e-pin/verify`, {
    method: 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ ePin }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to verify E-PIN.'));
  }
}

function accountResponseToClientAccount(account: AccountResponse): ClientAccount {
  return {
    id: account.iban,
    name: account.name,
    type: `${account.currency} account`,
    iban: account.iban,
    balance: Number(account.balance),
    currency: account.currency,
    status: account.status,
    opened: formatProfileDate(account.createdAt ?? null, 'Not available'),
    branch: 'Sofia Private Office',
  };
}

function getAccountStatusActionLabel(
  isUpdating: boolean,
  isBlocked: boolean,
  isSuspended: boolean,
) {
  if (isUpdating) return 'Updating...';
  if (isBlocked) return 'Blocked by admin';
  if (isSuspended) return 'Unlock account';
  return 'Lock account';
}

async function fetchAccounts(authSession: AuthSession): Promise<ClientAccount[]> {
  const response = await fetch(`${API_BASE_URL}/api/accounts`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to load accounts.'));
  }

  const accounts = await response.json() as AccountResponse[];
  return accounts.map(accountResponseToClientAccount);
}

async function createTopUpPaymentIntent(
  authSession: AuthSession,
  account: ClientAccount,
  amountCents: number,
): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/api/payments/create-intent`, {
    method: 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      accountIban: account.iban,
      amountCents,
      currency: account.currency,
    }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to start the top up.'));
  }

  const paymentIntent = await response.json() as PaymentIntentResponse;
  if (!paymentIntent.clientSecret) {
    throw new Error('Stripe did not return a payment client secret.');
  }
  return paymentIntent.clientSecret;
}

async function confirmTopUpPayment(authSession: AuthSession, paymentIntentId: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/payments/confirm`, {
    method: 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ paymentIntentId }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Stripe confirmed the card payment, but the account could not be credited.'));
  }
}

async function lookupRecipientAccount(authSession: AuthSession, iban: string): Promise<RecipientAccountResponse> {
  const response = await fetch(`${API_BASE_URL}/api/accounts/lookup?iban=${encodeURIComponent(iban)}`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Recipient IBAN was not found.'));
  }

  return response.json();
}

async function fetchTransactions(authSession: AuthSession): Promise<TransactionHistoryResponse[]> {
  const response = await fetch(`${API_BASE_URL}/api/v1/transactions`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to load transactions.'));
  }

  const transactionPage = await response.json() as TransactionPageResponse;
  return [...transactionPage.content].sort(
    (first, second) => new Date(second.timestamp).getTime() - new Date(first.timestamp).getTime(),
  );
}

async function submitTransfer(
  authSession: AuthSession,
  draft: TransferDraft,
): Promise<TransferResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/transactions/transfer`, {
    method: 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      sourceAccountIban: draft.sourceAccountIban,
      destinationAccountIban: draft.destinationAccountIban.trim().toUpperCase(),
      amount: Number(draft.amount),
      reason: draft.reason.trim(),
    }),
  });

  if (!response.ok) {
    let apiError: ApiErrorResponse | null = null;
    try {
      apiError = await response.json() as ApiErrorResponse;
    } catch {
      apiError = null;
    }
    const firstFieldError = apiError?.fieldErrors ? Object.values(apiError.fieldErrors)[0] : undefined;
    throw new ApiRequestError(
      firstFieldError ?? apiError?.message ?? 'Unable to send the transfer.',
      apiError?.fieldErrors,
    );
  }

  return response.json();
}

async function createBankAccount(
  authSession: AuthSession,
  name: string,
  currency: AccountCurrency,
): Promise<ClientAccount> {
  const response = await fetch(`${API_BASE_URL}/api/accounts`, {
    method: 'POST',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ name, currency }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to create account.'));
  }

  return accountResponseToClientAccount(await response.json() as AccountResponse);
}

async function updateBankAccountName(
  authSession: AuthSession,
  iban: string,
  name: string,
): Promise<ClientAccount> {
  const response = await fetch(`${API_BASE_URL}/api/accounts/${encodeURIComponent(iban)}`, {
    method: 'PUT',
    headers: {
      Authorization: getAuthorizationHeader(authSession),
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ name }),
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to update account name.'));
  }

  return accountResponseToClientAccount(await response.json() as AccountResponse);
}

async function updateBankAccountStatus(
  authSession: AuthSession,
  iban: string,
  action: 'suspend' | 'activate',
): Promise<ClientAccount> {
  const response = await fetch(`${API_BASE_URL}/api/users/accounts/${encodeURIComponent(iban)}/${action}`, {
    method: 'PUT',
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(
      response,
      action === 'suspend' ? 'Unable to lock account.' : 'Unable to unlock account.',
    ));
  }

  return accountResponseToClientAccount(await response.json() as AccountResponse);
}

async function fetchAdminUsers(authSession: AuthSession): Promise<AdminUserResponse[]> {
  const response = await fetch(`${API_BASE_URL}/api/admin/users`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to load users.'));
  }

  const page = await response.json() as ApiPage<AdminUserResponse>;
  return page.content ?? [];
}

async function fetchAdminTransactions(authSession: AuthSession): Promise<AdminTransactionResponse[]> {
  const response = await fetch(`${API_BASE_URL}/api/admin/transactions`, {
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(response, 'Unable to load transfer logs.'));
  }

  const page = await response.json() as ApiPage<AdminTransactionResponse>;
  return page.content ?? [];
}

async function updateAdminAccountStatus(
  authSession: AuthSession,
  iban: string,
  action: 'block' | 'unblock',
): Promise<AccountResponse> {
  const response = await fetch(`${API_BASE_URL}/api/admin/accounts/${encodeURIComponent(iban)}/${action}`, {
    method: 'PUT',
    headers: { Authorization: getAuthorizationHeader(authSession) },
  });

  if (!response.ok) {
    throw new Error(await getApiErrorMessage(
      response,
      action === 'block' ? 'Unable to block account.' : 'Unable to unblock account.',
    ));
  }

  return response.json();
}


async function verifyAdminSession(authSession: AuthSession): Promise<UserProfile> {
  const response = await fetch(`${API_BASE_URL}/api/admin/session`, {
    headers: {
      Authorization: `${authSession.tokenType} ${authSession.accessToken}`,
    },
  });

  if (!response.ok) {
    throw new Error('Admin access denied');
  }

  return response.json();
}

function getJwtExpiration(token: string): number | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window.atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload).exp || null;
  } catch {
    return null;
  }
}

function readStoredAuthSession(): AuthSession | null {
  try {
    const stored = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!stored) return null;
    
    const session = JSON.parse(stored) as AuthSession;
    return session;
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
}

function Header({
  theme,
  toggleTheme,
  openAuth,
  authSession,
  onLogout,
  showAdmin,
  showHome,
  showAccounts,
  showProfile,
  showTransactions,
  showPortfolio,
}: {
  theme: 'dark' | 'light';
  toggleTheme: () => void;
  openAuth: (mode: AuthMode) => void;
  authSession: AuthSession | null;
  onLogout: () => void;
  showAdmin: () => void;
  showHome: () => void;
  showAccounts: () => void;
  showProfile: () => void;
  showTransactions: () => void;
  showPortfolio: () => void;
}) {
  const ThemeIcon = theme === 'dark' ? Moon : Sun;

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b border-[rgb(var(--line))] bg-[rgb(var(--nav-bg))]/96 backdrop-blur">
      <nav className="flex h-[60px] items-center justify-between px-6 sm:px-10">
        <Logo onClick={showHome} />
        <div className="hidden items-center gap-10 text-sm font-semibold text-[rgb(var(--text-muted))] lg:flex">
          <button type="button" onClick={showAccounts} className="transition hover:text-[rgb(var(--text-strong))]">
            Accounts
          </button>
          <button type="button" onClick={showTransactions} className="transition hover:text-[rgb(var(--text-strong))]">
            Transactions
          </button>
          <button
            type="button"
            onClick={showPortfolio}
            className="transition hover:text-[rgb(var(--text-strong))]"
          >
            Portfolio
          </button>
          {authSession?.user.role === 'ADMIN' && (
            <button
              type="button"
              onClick={showAdmin}
              className="transition hover:text-[rgb(var(--text-strong))]"
            >
              Admin
            </button>
          )}
          <button
            type="button"
            onClick={() => {
              showHome();
              window.setTimeout(() => document.getElementById('contact')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
            }}
            className="transition hover:text-[rgb(var(--text-strong))]"
          >
            Contact
          </button>
        </div>
        <div className="flex items-center gap-5">
          {authSession ? (
            <div className="hidden items-center gap-3 sm:flex">
              <button
                type="button"
                onClick={showProfile}
                className="text-sm font-semibold text-[rgb(var(--text-muted))] underline decoration-[rgb(var(--gold))]/70 underline-offset-4 transition hover:text-[rgb(var(--text-strong))]"
              >
                {authSession.user.firstName} {authSession.user.lastName}
              </button>
              <button
                type="button"
                onClick={onLogout}
                className="text-sm font-semibold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
              >
                Logout
              </button>
            </div>
          ) : (
            <button
              type="button"
              onClick={() => openAuth('login')}
              className="hidden text-sm font-semibold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))] sm:inline"
            >
              Client Login
            </button>
          )}
          {authSession && (
            <button
              type="button"
              onClick={showProfile}
              className="grid h-10 w-10 place-items-center rounded-full border border-[rgb(var(--line-strong))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--gold))]"
              aria-label="Open user page"
              title="User page"
            >
              <UserRound size={18} strokeWidth={1.8} />
            </button>
          )}
          <button
            type="button"
            onClick={() => authSession ? showAccounts() : openAuth('register')}
            className="rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-bold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 hover:brightness-105"
          >
            {authSession ? 'Accounts' : 'Inquire'}
          </button>
        </div>
      </nav>
      <button
        type="button"
        onClick={toggleTheme}
        className="absolute right-6 top-[72px] grid h-10 w-10 place-items-center rounded-full border border-[rgb(var(--line-strong))] bg-[rgb(var(--float-bg))] text-[rgb(var(--gold))] shadow-vault transition hover:-translate-y-0.5 sm:right-10"
        aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}
      >
        <ThemeIcon size={20} strokeWidth={1.8} />
      </button>
    </header>
  );
}

function PortfolioCard() {
  return (
    <aside id="portfolio" className="w-full max-w-[422px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault">
      <div className="mb-6 flex items-center justify-between border-b border-[rgb(var(--line))] pb-4">
        <p className="text-[0.64rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--text-muted))]">
          Portfolio Overview
        </p>
        <span className="text-[0.68rem] font-bold uppercase tracking-[0.16em] text-[rgb(var(--gold))]">Live</span>
      </div>
      <div>
        <p className="font-display text-[1.45rem] font-bold text-[rgb(var(--text-strong))]">$4,821,390.44</p>
        <div className="mt-3 flex items-center gap-3">
          <span className="rounded bg-emerald-500/15 px-2 py-1 text-xs font-extrabold text-emerald-500">+2.34%</span>
          <span className="text-sm font-semibold text-[rgb(var(--text-muted))]">today</span>
        </div>
      </div>
      <div className="mt-7 space-y-4">
        {[
          ['Equities', '$2.51M', '52%'],
          ['Fixed Income', '$1.35M', '28%'],
          ['Alternatives', '$964K', '20%'],
        ].map(([label, value, width]) => (
          <div key={label}>
            <div className="mb-2 flex items-center justify-between text-xs font-semibold">
              <span className="text-[rgb(var(--text-muted))]">{label}</span>
              <span className="text-[rgb(var(--text-strong))]">{value}</span>
            </div>
            <div className="h-1 rounded-full bg-[rgb(var(--line))]">
              <div className="h-full rounded-full bg-[rgb(var(--gold))]" style={{ width }} />
            </div>
          </div>
        ))}
      </div>
    </aside>
  );
}

function Hero({ showTransactions, showPortfolio }: { showTransactions: () => void; showPortfolio: () => void }) {
  return (
    <section className="pattern-bg relative flex min-h-screen items-start overflow-hidden px-6 pb-20 pt-32 sm:px-10 lg:items-center lg:pt-[72px]">
      <div className="absolute inset-x-0 bottom-0 h-1/2 bg-[radial-gradient(circle_at_31%_78%,rgba(var(--hero-glow),0.55),transparent_34%)]" />
      <div className="relative mx-auto grid w-full max-w-[980px] items-center gap-16 lg:grid-cols-[1fr_422px] lg:gap-[92px] lg:translate-y-10">
        <div className="max-w-[466px]">
          <Eyebrow>Private Banking Since 1847</Eyebrow>
          <h1 className="mt-9 font-display text-[clamp(3rem,12vw,3.45rem)] font-semibold leading-[0.98] text-[rgb(var(--text-strong))] sm:text-[clamp(3.5rem,3.25vw,4.15rem)]">
            <span className="block sm:whitespace-nowrap">Wealth Preserved</span>
            <span className="block">Through</span>
            <span className="block text-[rgb(var(--gold))]">Generations</span>
          </h1>
          <p className="mt-10 max-w-[466px] text-lg leading-8 text-[rgb(var(--text-muted))]">
            For over 175 years, distinguished families have entrusted SAFE Bank with their legacy. We offer the discretion, expertise, and unwavering commitment your wealth deserves.
          </p>
          <div className="mt-10 flex flex-col gap-4 sm:flex-row">
            <button
              type="button"
              onClick={showTransactions}
              className="rounded-md bg-[rgb(var(--gold))] px-8 py-4 text-center text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
            >
              Transfer
            </button>
            <button
              type="button"
              onClick={showPortfolio}
              className="rounded-md border border-[rgb(var(--button-line))] px-8 py-4 text-center text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
            >
              Portfolio
            </button>
          </div>
        </div>
        <div className="flex justify-center lg:justify-end">
          <PortfolioCard />
        </div>
      </div>
    </section>
  );
}

function StatsBand() {
  return (
    <section className="border-y border-[rgb(var(--line))] bg-[rgb(var(--stat-bg))]">
      <div className="mx-auto grid max-w-[980px] grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <div key={stat.label} className={`px-6 py-7 text-center ${index !== 0 ? 'lg:border-l' : ''} border-[rgb(var(--line))]`}>
            <p className="font-display text-[2rem] font-bold leading-none text-[rgb(var(--text-strong))] md:text-[2.35rem]">{stat.value}</p>
            <p className="mt-3 text-[0.64rem] font-extrabold uppercase tracking-[0.28em] text-[rgb(var(--text-muted))]">{stat.label}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

function formatAccountBalance(account: ClientAccount) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: account.currency,
    minimumFractionDigits: 2,
  }).format(account.balance);
}

function formatCurrencyAmount(value: number, currency: string) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(value);
}

function getPortfolioTotalLabel(accounts: ClientAccount[]) {
  if (accounts.length === 0) return formatCurrencyAmount(0, 'EUR');

  const currencies = new Set(accounts.map((account) => account.currency));
  if (currencies.size > 1) {
    return 'Multi-currency';
  }

  const [currency] = currencies;
  const total = accounts.reduce((sum, account) => sum + account.balance, 0);
  return formatCurrencyAmount(total, currency);
}

function getPortfolioRiskProfile(accounts: ClientAccount[]) {
  if (accounts.length === 0) return 'Not set';
  if (accounts.length === 1) return 'Cash';
  return new Set(accounts.map((account) => account.currency)).size > 1 ? 'Diversified cash' : 'Cash reserve';
}

function getPortfolioHoldings(accounts: ClientAccount[]): AccountPortfolioHolding[] {
  const positiveTotal = accounts.reduce((sum, account) => sum + Math.max(account.balance, 0), 0);

  return accounts.map((account) => {
    const allocation = positiveTotal > 0 ? Math.round((Math.max(account.balance, 0) / positiveTotal) * 100) : 0;
    const allocationLabel = `${allocation}%`;

    return {
      account,
      name: account.name,
      category: `${account.currency} Account`,
      value: formatAccountBalance(account),
      allocation: allocationLabel,
      allocationWidth: allocationLabel,
      status: account.balance > 0 ? 'Funded' : 'Awaiting funds',
    };
  });
}

function getPortfolioCurrencyAllocations(accounts: ClientAccount[]): PortfolioHolding[] {
  const totalsByCurrency = accounts.reduce((totals, account) => {
    totals.set(account.currency, (totals.get(account.currency) ?? 0) + account.balance);
    return totals;
  }, new Map<string, number>());
  const positiveTotal = Array.from(totalsByCurrency.values()).reduce((sum, value) => sum + Math.max(value, 0), 0);

  return Array.from(totalsByCurrency.entries())
    .sort(([firstCurrency], [secondCurrency]) => firstCurrency.localeCompare(secondCurrency))
    .map(([currency, value]) => {
      const allocation = positiveTotal > 0 ? Math.round((Math.max(value, 0) / positiveTotal) * 100) : 0;
      const allocationLabel = `${allocation}%`;

      return {
        name: currency,
        category: currency,
        value: formatCurrencyAmount(value, currency),
        allocation: allocationLabel,
        allocationWidth: allocationLabel,
        status: `${currency} balance`,
      };
    });
}

function maskIban(iban: string) {
  return `${iban.slice(0, 4)} •••• •••• ${iban.slice(-4)}`;
}

function getTopUpAmountCents(value: string) {
  const match = /^(\d+)(?:\.(\d{1,2}))?$/.exec(value.trim());
  if (!match) return null;

  const wholeAmount = Number(match[1]);
  const decimalAmount = Number((match[2] ?? '').padEnd(2, '0'));
  const amountCents = (wholeAmount * 100) + decimalAmount;
  return Number.isSafeInteger(amountCents) && amountCents > 0 ? amountCents : null;
}

type TopUpFormProps = Readonly<{
  account: ClientAccount;
  authSession: AuthSession;
  onClose: () => void;
  onPaymentConfirmed: () => Promise<void>;
}>;

function TopUpForm({ account, authSession, onClose, onPaymentConfirmed }: TopUpFormProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [amount, setAmount] = React.useState('');
  const [isCardComplete, setIsCardComplete] = React.useState(false);
  const [isPaying, setIsPaying] = React.useState(false);
  const [paymentError, setPaymentError] = React.useState('');
  const [paymentSuccess, setPaymentSuccess] = React.useState('');
  const isLightTheme = typeof document !== 'undefined' && document.documentElement.dataset.theme === 'light';
  const cardOptions = {
    hidePostalCode: true,
    style: {
      base: {
        color: isLightTheme ? '#181915' : '#f3f1ea',
        fontFamily: 'Inter, system-ui, sans-serif',
        fontSize: '16px',
        fontSmoothing: 'antialiased',
        '::placeholder': {
          color: isLightTheme ? '#777b75' : '#8b8b87',
        },
      },
      invalid: {
        color: '#ef4444',
        iconColor: '#ef4444',
      },
    },
  } satisfies StripeCardElementOptions;

  async function pay(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPaymentError('');

    const amountCents = getTopUpAmountCents(amount);
    if (!amountCents) {
      setPaymentError('Enter an amount greater than zero with no more than two decimal places.');
      return;
    }
    if (!stripe || !elements) {
      setPaymentError('The secure payment form is still loading.');
      return;
    }

    const card = elements.getElement(CardElement);
    if (!card || !isCardComplete) {
      setPaymentError('Enter complete card details.');
      return;
    }

    setIsPaying(true);
    try {
      const clientSecret = await createTopUpPaymentIntent(authSession, account, amountCents);
      const confirmation = await stripe.confirmCardPayment(clientSecret, {
        payment_method: { card },
      });

      if (confirmation.error) {
        setPaymentError(confirmation.error.message ?? 'Stripe could not confirm the payment.');
        return;
      }
      if (confirmation.paymentIntent?.status !== 'succeeded') {
        setPaymentError('The payment was not completed. Please try again.');
        return;
      }

      await confirmTopUpPayment(authSession, confirmation.paymentIntent.id);
      await onPaymentConfirmed();
      setPaymentSuccess(`${formatCurrencyAmount(amountCents / 100, account.currency)} was added successfully.`);
    } catch (error) {
      setPaymentError(error instanceof Error ? error.message : 'Unable to complete the top up.');
    } finally {
      setIsPaying(false);
    }
  }

  if (paymentSuccess) {
    return (
      <div className="mt-7">
        <output className="block rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm font-bold text-emerald-500">
          {paymentSuccess}
        </output>
        <button
          type="button"
          onClick={onClose}
          className="mt-5 w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold"
        >
          Done
        </button>
      </div>
    );
  }

  return (
    <form className="mt-7 space-y-5" onSubmit={pay}>
      <label className="block">
        <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
          Amount ({account.currency})
        </span>
        <input
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          inputMode="decimal"
          autoFocus
          placeholder="0.00"
          disabled={isPaying}
          className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] disabled:opacity-60"
        />
      </label>
      <div>
        <p className="mb-2 text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Card details</p>
        <div className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3.5 focus-within:border-[rgb(var(--gold))]">
          <CardElement
            options={cardOptions}
            onChange={(event) => {
              setIsCardComplete(event.complete);
              if (event.error) setPaymentError(event.error.message);
            }}
          />
        </div>
        <p className="mt-2 text-xs font-semibold leading-5 text-[rgb(var(--text-muted))]">
          {account.currency === 'BGN'
            ? 'Card details are handled securely by Stripe. BGN top-ups are charged in EUR at the fixed conversion rate.'
            : 'Card details are handled securely by Stripe.'}
        </p>
      </div>
      {paymentError && <p className="text-sm font-bold text-red-500" role="alert">{paymentError}</p>}
      <button
        type="submit"
        disabled={isPaying || !stripe || !elements || !isCardComplete}
        className="w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
      >
        {isPaying ? 'Processing...' : 'Pay'}
      </button>
      <button
        type="button"
        onClick={onClose}
        disabled={isPaying}
        className="w-full rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60"
      >
        Cancel
      </button>
    </form>
  );
}

type TopUpModalProps = Readonly<{
  account: ClientAccount;
  authSession: AuthSession;
  onClose: () => void;
  onPaymentConfirmed: () => Promise<void>;
}>;

function TopUpModal({ account, authSession, onClose, onPaymentConfirmed }: TopUpModalProps) {
  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center px-5 py-8">
      <button type="button" aria-label="Close top up popup" className="absolute inset-0 bg-black/65 backdrop-blur-sm" onClick={onClose} />
      <dialog
        open
        aria-labelledby="top-up-title"
        className="relative max-h-[calc(100vh-4rem)] w-full max-w-[480px] overflow-y-auto rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--text-strong))]"
          aria-label="Close top up popup"
        >
          <X size={17} strokeWidth={1.8} />
        </button>
        <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <CreditCard size={20} strokeWidth={1.8} />
        </div>
        <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.3em] text-[rgb(var(--gold))]">Secure Card Payment</p>
        <h2 id="top-up-title" className="mt-3 font-display text-4xl font-semibold text-[rgb(var(--text-strong))]">Top up account</h2>
        <p className="mt-3 text-sm font-semibold leading-6 text-[rgb(var(--text-muted))]">
          {account.name} · {maskIban(account.iban)} · {account.currency}
        </p>

        {stripePromise ? (
          <Elements stripe={stripePromise} options={{ locale: 'en' }}>
            <TopUpForm
              account={account}
              authSession={authSession}
              onClose={onClose}
              onPaymentConfirmed={onPaymentConfirmed}
            />
          </Elements>
        ) : (
          <div className="mt-7">
            <p className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm font-bold leading-6 text-red-500" role="alert">
              Stripe is not configured. Set VITE_STRIPE_PUBLISHABLE_KEY and restart the frontend.
            </p>
            <button type="button" onClick={onClose} className="mt-5 w-full rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))]">
              Close
            </button>
          </div>
        )}
      </dialog>
    </div>
  );
}

function AccountsPage({
  showHome,
  showTransactions,
  authSession,
}: {
  showHome: () => void;
  showTransactions: () => void;
  authSession: AuthSession;
}) {
  const [accounts, setAccounts] = React.useState<ClientAccount[]>([]);
  const [selectedAccountId, setSelectedAccountId] = React.useState<string | null>(null);
  const [isLoadingAccounts, setIsLoadingAccounts] = React.useState(true);
  const [accountsError, setAccountsError] = React.useState('');
  const [accountActionError, setAccountActionError] = React.useState('');
  const [accountSuccess, setAccountSuccess] = React.useState('');
  const [isCreateAccountOpen, setIsCreateAccountOpen] = React.useState(false);
  const [isCreatingAccount, setIsCreatingAccount] = React.useState(false);
  const [createAccountError, setCreateAccountError] = React.useState('');
  const [editingName, setEditingName] = React.useState(false);
  const [draftName, setDraftName] = React.useState('');
  const [isSavingName, setIsSavingName] = React.useState(false);
  const [isUpdatingAccountStatus, setIsUpdatingAccountStatus] = React.useState(false);
  const [isIbanVisible, setIsIbanVisible] = React.useState(false);
  const [copiedAccountId, setCopiedAccountId] = React.useState<string | null>(null);
  const [topUpAccount, setTopUpAccount] = React.useState<ClientAccount | null>(null);

  const selectedAccount = accounts.find((account) => account.id === selectedAccountId) ?? accounts[0] ?? null;
  const selectedName = selectedAccount?.name ?? '';
  const isSelectedAccountActive = selectedAccount?.status === 'ACTIVE';
  const isSelectedAccountSuspended = selectedAccount?.status === 'SUSPENDED';
  const isSelectedAccountBlocked = selectedAccount?.status === 'BLOCKED';
  const selectedAccountStatusActionLabel = getAccountStatusActionLabel(
    isUpdatingAccountStatus,
    isSelectedAccountBlocked,
    isSelectedAccountSuspended,
  );
  const IbanVisibilityIcon = isIbanVisible ? EyeOff : Eye;

  const loadAccounts = React.useCallback(async (preferredAccountId?: string) => {
    setIsLoadingAccounts(true);
    setAccountsError('');
    try {
      const loadedAccounts = await fetchAccounts(authSession);
      setAccounts(loadedAccounts);
      setSelectedAccountId((currentId) => {
        if (preferredAccountId && loadedAccounts.some((account) => account.id === preferredAccountId)) {
          return preferredAccountId;
        }
        if (currentId && loadedAccounts.some((account) => account.id === currentId)) {
          return currentId;
        }
        return loadedAccounts[0]?.id ?? null;
      });
    } catch (loadError) {
      setAccountsError(loadError instanceof Error ? loadError.message : 'Unable to load accounts.');
    } finally {
      setIsLoadingAccounts(false);
    }
  }, [authSession]);

  React.useEffect(() => {
    runAsyncAction(() => loadAccounts());
  }, [loadAccounts]);

  function selectAccount(account: ClientAccount) {
    setSelectedAccountId(account.id);
    setEditingName(false);
    setIsIbanVisible(false);
    setAccountActionError('');
  }

  function openTopUp(account: ClientAccount) {
    selectAccount(account);
    setAccountSuccess('');
    setTopUpAccount(account);
  }

  async function refreshAfterTopUp(account: ClientAccount) {
    await loadAccounts(account.id);
    setAccountSuccess(`Top up for ${account.name} was confirmed.`);
  }

  function startRenaming() {
    if (!selectedAccount) return;
    setDraftName(selectedName);
    setEditingName(true);
    setAccountActionError('');
  }

  async function saveAccountName(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedAccount) return;

    const nextName = draftName.trim();
    if (!nextName) return;

    setIsSavingName(true);
    setAccountActionError('');
    setAccountSuccess('');
    try {
      const updatedAccount = await updateBankAccountName(authSession, selectedAccount.iban, nextName);
      setAccounts((current) => current.map((account) => (
        account.id === updatedAccount.id ? updatedAccount : account
      )));
      setEditingName(false);
      setAccountSuccess('Account name updated.');
    } catch (renameError) {
      setAccountActionError(renameError instanceof Error ? renameError.message : 'Unable to update account name.');
    } finally {
      setIsSavingName(false);
    }
  }

  async function copyIban() {
    if (!selectedAccount) return;
    await navigator.clipboard.writeText(selectedAccount.iban);
    setCopiedAccountId(selectedAccount.id);
    window.setTimeout(() => setCopiedAccountId(null), 1800);
  }

  async function createAccount(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const accountName = getFormString(formData, 'accountName', true);
    const currency = getFormString(formData, 'currency') as AccountCurrency;
    if (!accountName) {
      setCreateAccountError('Account name is required.');
      return;
    }
    if (!ACCOUNT_CURRENCIES.includes(currency)) {
      setCreateAccountError('Choose a supported currency.');
      return;
    }

    setIsCreatingAccount(true);
    setCreateAccountError('');
    setAccountSuccess('');
    try {
      const createdAccount = await createBankAccount(authSession, accountName, currency);
      await loadAccounts(createdAccount.id);
      setIsCreateAccountOpen(false);
      setEditingName(false);
      setIsIbanVisible(false);
      setAccountSuccess('Account created successfully.');
    } catch (createError) {
      setCreateAccountError(createError instanceof Error ? createError.message : 'Unable to create account.');
    } finally {
      setIsCreatingAccount(false);
    }
  }

  async function toggleSelectedAccountLock() {
    if (!selectedAccount) return;
    if (isSelectedAccountBlocked) {
      setAccountActionError('Blocked accounts cannot be unlocked by the account owner.');
      return;
    }

    const action = isSelectedAccountSuspended ? 'activate' : 'suspend';
    setIsUpdatingAccountStatus(true);
    setAccountActionError('');
    setAccountSuccess('');
    try {
      const updatedAccount = await updateBankAccountStatus(authSession, selectedAccount.iban, action);
      setAccounts((current) => current.map((account) => (
        account.id === updatedAccount.id ? updatedAccount : account
      )));
      setAccountSuccess(action === 'suspend' ? 'Account locked.' : 'Account unlocked.');
    } catch (statusError) {
      setAccountActionError(statusError instanceof Error ? statusError.message : 'Unable to update account status.');
    } finally {
      setIsUpdatingAccountStatus(false);
    }
  }

  return (
    <section className="pattern-bg min-h-screen px-6 pb-20 pt-32 sm:px-10 lg:pt-36">
      <div className="mx-auto max-w-[1100px]">
        <button
          type="button"
          onClick={showHome}
          className="inline-flex items-center gap-2 text-sm font-bold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
        >
          <ArrowLeft size={16} strokeWidth={1.8} />
          Back to overview
        </button>

        <div className="mt-10 flex flex-col justify-between gap-8 border-b border-[rgb(var(--line))] pb-10 md:flex-row md:items-end">
          <div>
            <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Private Banking</p>
            <h1 className="mt-4 font-display text-[clamp(3rem,5vw,4.5rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
              Your Accounts
            </h1>
            <p className="mt-5 max-w-[610px] text-base leading-7 text-[rgb(var(--text-muted))]">
              Review balances, account details, and the accounts available for transfers.
            </p>
          </div>
          <div className="flex flex-col gap-3 sm:flex-row">
            <button
              type="button"
              onClick={() => setIsCreateAccountOpen(true)}
              className="inline-flex items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
            >
              <Plus size={17} strokeWidth={1.8} />
              Create account
            </button>
            <button
              type="button"
              onClick={showTransactions}
              className="inline-flex items-center justify-center gap-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
            >
              <Zap size={17} strokeWidth={1.8} />
              New Transfer
            </button>
          </div>
        </div>

        {accountSuccess && <output className="mt-5 block text-sm font-bold text-emerald-500">{accountSuccess}</output>}
        {accountActionError && <p className="mt-5 text-sm font-bold text-red-500" role="alert">{accountActionError}</p>}

        <div className="grid gap-px border-b border-[rgb(var(--line))] bg-[rgb(var(--line))] sm:grid-cols-2">
          {[
            ['Accounts', String(accounts.length)],
            ['Currencies', String(new Set(accounts.map((account) => account.currency)).size)],
          ].map(([label, value]) => (
            <div key={label} className="bg-[rgb(var(--page-bg))] px-6 py-7">
              <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.25em] text-[rgb(var(--text-muted))]">{label}</p>
              <p className="mt-3 font-display text-2xl font-bold text-[rgb(var(--text-strong))]">{value}</p>
            </div>
          ))}
        </div>

        {isLoadingAccounts ? (
          <div className="mt-10 rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-8 text-sm font-bold text-[rgb(var(--text-muted))]">
            Loading accounts...
          </div>
        ) : accountsError ? (
          <div className="mt-10 rounded-lg border border-red-500/30 bg-red-500/10 p-8">
            <p className="text-sm font-bold text-red-500" role="alert">{accountsError}</p>
            <button
              type="button"
              onClick={() => runAsyncAction(() => loadAccounts())}
              className="mt-5 rounded-md border border-[rgb(var(--button-line))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
            >
              Retry
            </button>
          </div>
        ) : !selectedAccount ? (
          <div className="mt-10 rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-8 text-center shadow-vault">
            <div className="mx-auto grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
              <CreditCard size={20} strokeWidth={1.8} />
            </div>
            <h2 className="mt-5 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">No accounts yet</h2>
            <p className="mx-auto mt-3 max-w-[420px] text-sm leading-6 text-[rgb(var(--text-muted))]">
              Create your first account to receive a backend-generated IBAN and start using transfers.
            </p>
            <button
              type="button"
              onClick={() => setIsCreateAccountOpen(true)}
              className="mt-6 inline-flex items-center justify-center gap-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
            >
              <Plus size={17} strokeWidth={1.8} />
              Create account
            </button>
          </div>
        ) : (
          <div className="mt-10 grid gap-8 lg:grid-cols-[360px_1fr]">
            <div className="overflow-hidden rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))]">
              <div className="border-b border-[rgb(var(--line))] px-5 py-4">
                <p className="text-[0.64rem] font-extrabold uppercase tracking-[0.28em] text-[rgb(var(--text-muted))]">Account Directory</p>
              </div>
              <div className="divide-y divide-[rgb(var(--line))]">
                {accounts.map((account) => {
                  const isSelected = account.id === selectedAccount.id;
                  const isLocked = account.status !== 'ACTIVE';
                  const AccountIcon = isLocked ? LockKeyhole : CreditCard;
                  return (
                    <div
                      key={account.id}
                      className={`transition ${
                        isSelected
                          ? 'bg-[rgb(var(--icon-bg))]'
                          : 'hover:bg-[rgb(var(--service-hover))]'
                      }`}
                    >
                      <button
                        type="button"
                        onClick={() => selectAccount(account)}
                        className="w-full px-5 pb-3 pt-5 text-left"
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="min-w-0">
                            <p className="truncate font-bold text-[rgb(var(--text-strong))]">{account.name}</p>
                            <p className="mt-1 text-xs font-semibold text-[rgb(var(--text-muted))]">{account.type}</p>
                          </div>
                          <AccountIcon
                            size={18}
                            strokeWidth={1.7}
                            className={isSelected ? 'text-[rgb(var(--gold))]' : 'text-[rgb(var(--text-muted))]'}
                          />
                        </div>
                        <p className="mt-5 font-display text-2xl font-bold text-[rgb(var(--text-strong))]">{formatAccountBalance(account)}</p>
                        <div className="mt-2 flex flex-wrap items-center gap-2">
                          <p className="text-xs font-semibold tracking-[0.08em] text-[rgb(var(--text-muted))]">{maskIban(account.iban)}</p>
                          {account.status !== 'ACTIVE' && (
                            <span className="rounded-full border border-[rgb(var(--line))] px-2 py-0.5 text-[0.62rem] font-extrabold uppercase tracking-[0.14em] text-[rgb(var(--gold))]">
                              {account.status}
                            </span>
                          )}
                        </div>
                      </button>
                      <div className="px-5 pb-5">
                        <button
                          type="button"
                          onClick={() => openTopUp(account)}
                          disabled={isLocked}
                          className="inline-flex w-full items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-4 py-2.5 text-xs font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-45"
                        >
                          <Plus size={15} strokeWidth={2} />
                          Top up
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <article className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault sm:p-8">
              <div className="flex flex-col justify-between gap-6 border-b border-[rgb(var(--line))] pb-7 sm:flex-row sm:items-start">
                <div>
                  {editingName ? (
                    <form className="flex flex-col gap-3 sm:flex-row" onSubmit={saveAccountName}>
                      <input
                        value={draftName}
                        onChange={(event) => setDraftName(event.target.value)}
                        maxLength={80}
                        autoFocus
                        aria-label="Account name"
                        className="min-w-0 rounded-md border border-[rgb(var(--gold))] bg-[rgb(var(--page-bg))] px-4 py-2.5 text-lg font-bold text-[rgb(var(--text-strong))] outline-none"
                      />
                      <div className="flex gap-2">
                        <button type="submit" disabled={isSavingName} className="grid h-11 w-11 place-items-center rounded-md bg-[rgb(var(--gold))] text-[rgb(var(--gold-ink))] disabled:cursor-not-allowed disabled:opacity-60" aria-label="Save account name">
                          <Check size={17} strokeWidth={2} />
                        </button>
                        <button type="button" onClick={() => setEditingName(false)} disabled={isSavingName} className="grid h-11 w-11 place-items-center rounded-md border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] disabled:cursor-not-allowed disabled:opacity-60" aria-label="Cancel account name edit">
                          <X size={17} strokeWidth={1.8} />
                        </button>
                      </div>
                    </form>
                  ) : (
                    <div className="flex items-center gap-3">
                      <h2 className="font-display text-3xl font-semibold text-[rgb(var(--text-strong))] sm:text-4xl">{selectedName}</h2>
                      <button
                        type="button"
                        onClick={startRenaming}
                        className="grid h-9 w-9 shrink-0 place-items-center rounded-md border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--gold))]"
                        aria-label="Rename account"
                        title="Rename account"
                      >
                        <Pencil size={15} strokeWidth={1.8} />
                      </button>
                    </div>
                  )}
                  <p className="mt-2 text-sm font-semibold text-[rgb(var(--text-muted))]">{selectedAccount.type}</p>
                </div>
                <div className="sm:text-right">
                  <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.25em] text-[rgb(var(--text-muted))]">Available Balance</p>
                  <p className="mt-2 font-display text-3xl font-bold text-[rgb(var(--text-strong))]">{formatAccountBalance(selectedAccount)}</p>
                </div>
              </div>

              <div className="py-7">
                <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.25em] text-[rgb(var(--text-muted))]">IBAN</p>
                <div className="mt-3 flex flex-wrap items-center gap-3">
                  <p className="break-all font-mono text-base font-bold tracking-[0.08em] text-[rgb(var(--text-strong))]">
                    {isIbanVisible ? selectedAccount.iban : maskIban(selectedAccount.iban)}
                  </p>
                  <button
                    type="button"
                    onClick={() => setIsIbanVisible((current) => !current)}
                    className="grid h-9 w-9 place-items-center rounded-md border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--gold))]"
                    aria-label={isIbanVisible ? 'Hide IBAN' : 'Show IBAN'}
                    title={isIbanVisible ? 'Hide IBAN' : 'Show IBAN'}
                  >
                    <IbanVisibilityIcon size={16} strokeWidth={1.8} />
                  </button>
                  <button
                    type="button"
                    onClick={copyIban}
                    className="grid h-9 w-9 place-items-center rounded-md border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--gold))]"
                    aria-label="Copy IBAN"
                    title="Copy IBAN"
                  >
                    {copiedAccountId === selectedAccount.id
                      ? <Check size={16} strokeWidth={2} />
                      : <Copy size={16} strokeWidth={1.8} />}
                  </button>
                </div>
              </div>

              <dl className="grid overflow-hidden rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] sm:grid-cols-2">
                {[
                  ['Currency', selectedAccount.currency],
                  ['Status', selectedAccount.status],
                  ['Account holder', 'Primary client'],
                  ['Opened', selectedAccount.opened],
                  ['Servicing branch', selectedAccount.branch],
                ].map(([label, value], index) => (
                  <div
                    key={label}
                    className={`bg-[rgb(var(--page-bg))] px-5 py-4 ${
                      index < 3 ? 'border-b border-[rgb(var(--line))]' : ''
                    } ${index % 2 === 0 ? 'sm:border-r sm:border-[rgb(var(--line))]' : ''}`}
                  >
                    <dt className="text-[0.6rem] font-extrabold uppercase tracking-[0.22em] text-[rgb(var(--text-muted))]">{label}</dt>
                    <dd className="mt-2 text-sm font-bold text-[rgb(var(--text-strong))]">{value}</dd>
                  </div>
                ))}
              </dl>

              <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                <button
                  type="button"
                  onClick={showTransactions}
                  disabled={!isSelectedAccountActive}
                  className="inline-flex items-center justify-center gap-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-45 disabled:hover:translate-y-0"
                >
                  <Zap size={16} strokeWidth={1.8} />
                  {isSelectedAccountActive ? 'Transfer from this account' : 'Account locked'}
                </button>
                <button
                  type="button"
                  onClick={startRenaming}
                  className="inline-flex items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
                >
                  <Pencil size={16} strokeWidth={1.8} />
                  Edit account name
                </button>
                <button
                  type="button"
                  onClick={() => runAsyncAction(toggleSelectedAccountLock)}
                  disabled={isUpdatingAccountStatus || isSelectedAccountBlocked}
                  className="inline-flex items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSelectedAccountSuspended
                    ? <Unlock size={16} strokeWidth={1.8} />
                    : <LockKeyhole size={16} strokeWidth={1.8} />}
                  {selectedAccountStatusActionLabel}
                </button>
              </div>
            </article>
          </div>
        )}
      </div>

      {isCreateAccountOpen && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center px-5 py-8">
          <button
            type="button"
            aria-label="Close create account popup"
            className="absolute inset-0 bg-black/65 backdrop-blur-sm"
            onClick={() => setIsCreateAccountOpen(false)}
          />
          <dialog
            open
            aria-labelledby="create-account-title"
            className="relative w-full max-w-[460px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
          >
            <button
              type="button"
              onClick={() => setIsCreateAccountOpen(false)}
              className="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--text-strong))]"
              aria-label="Close popup"
            >
              <X size={17} strokeWidth={1.8} />
            </button>
            <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
              <Plus size={20} strokeWidth={1.8} />
            </div>
            <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.3em] text-[rgb(var(--gold))]">Account Management</p>
            <h2 id="create-account-title" className="mt-3 font-display text-4xl font-semibold text-[rgb(var(--text-strong))]">
              Create Account
            </h2>
            <form className="mt-7 space-y-4" onSubmit={createAccount}>
              <label className="block">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Account Name</span>
                <input
                  name="accountName"
                  type="text"
                  maxLength={80}
                  autoFocus
                  className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                  placeholder="Main Account"
                  required
                />
              </label>
              <label className="block">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Currency</span>
                <select
                  name="currency"
                  className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]"
                  required
                >
                  {ACCOUNT_CURRENCIES.map((currency) => (
                    <option key={currency} value={currency}>{currency}</option>
                  ))}
                </select>
              </label>
              {createAccountError && <p className="text-sm font-bold text-red-500" role="alert">{createAccountError}</p>}
              <button
                type="submit"
                disabled={isCreatingAccount}
                className="w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
              >
                {isCreatingAccount ? 'Creating...' : 'Create account'}
              </button>
              <button
                type="button"
                onClick={() => setIsCreateAccountOpen(false)}
                disabled={isCreatingAccount}
                className="w-full rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60"
              >
                Cancel
              </button>
            </form>
          </dialog>
        </div>
      )}
      {topUpAccount && (
        <TopUpModal
          account={topUpAccount}
          authSession={authSession}
          onClose={() => setTopUpAccount(null)}
          onPaymentConfirmed={() => refreshAfterTopUp(topUpAccount)}
        />
      )}
    </section>
  );
}

function formatProfileDate(value: string | null, fallback: string) {
  if (!value) return fallback;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return fallback;
  return new Intl.DateTimeFormat('en-US', { dateStyle: 'long' }).format(date);
}

function SecureEPinField({
  label,
  name,
  value,
  isVisible,
  onChange,
  onToggleVisibility,
}: Readonly<{
  label: string;
  name: string;
  value: string;
  isVisible: boolean;
  onChange: (value: string) => void;
  onToggleVisibility: () => void;
}>) {
  return (
    <label className="block">
      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">{label}</span>
      <div className="relative">
        <input
          name={name}
          type={isVisible ? 'text' : 'password'}
          inputMode="numeric"
          autoComplete="off"
          minLength={EPIN_LENGTH}
          maxLength={EPIN_LENGTH}
          pattern={EPIN_INPUT_PATTERN}
          value={value}
          onChange={(event) => onChange(sanitizeEPin(event.target.value))}
          className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 pr-12 font-mono text-sm font-semibold tracking-[0.18em] text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]"
          required
        />
        <button
          type="button"
          onClick={onToggleVisibility}
          className="absolute right-3 top-1/2 grid h-8 w-8 -translate-y-1/2 place-items-center rounded-full text-[rgb(var(--text-muted))] transition hover:bg-[rgb(var(--line))] hover:text-[rgb(var(--text-strong))]"
          aria-label={isVisible ? `Hide ${label}` : `Show ${label}`}
          aria-pressed={isVisible}
        >
          {isVisible ? <EyeOff size={17} /> : <Eye size={17} />}
        </button>
      </div>
    </label>
  );
}

function validateEPinSave(currentPassword: string, currentEPin: string | null, newEPin: string) {
  if (!currentPassword) return 'Enter your current password.';
  if (currentEPin !== null && !EPIN_PATTERN.test(currentEPin)) return `Current ${EPIN_VALIDATION_MESSAGE}`;
  if (!EPIN_PATTERN.test(newEPin)) return `New ${EPIN_VALIDATION_MESSAGE}`;
  if (currentEPin !== null && currentEPin === newEPin) return 'New E-PIN must be different from the current E-PIN.';
  return '';
}

function ChangeEPinModal({
  authSession,
  isSet,
  onClose,
  onSaved,
}: Readonly<{
  authSession: AuthSession;
  isSet: boolean;
  onClose: () => void;
  onSaved: () => void;
}>) {
  const [error, setError] = React.useState('');
  const [isSubmitting, setIsSubmitting] = React.useState(false);
  const [currentEPin, setCurrentEPin] = React.useState('');
  const [newEPin, setNewEPin] = React.useState('');
  const [showCurrentEPin, setShowCurrentEPin] = React.useState(false);
  const [showNewEPin, setShowNewEPin] = React.useState(false);
  const actionLabel = isSet ? 'Change E-PIN' : 'Set E-PIN';
  const submitLabel = isSubmitting ? 'Saving...' : actionLabel;
  const description = isSet
    ? 'Confirm your current password and E-PIN before setting a new 6-digit E-PIN.'
    : 'Confirm your current password before setting your 6-digit E-PIN.';

  React.useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !isSubmitting) onClose();
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleKeyDown);
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isSubmitting, onClose]);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');

    const formData = new FormData(event.currentTarget);
    const currentPassword = getFormString(formData, 'currentPassword');
    const currentEPinValue = isSet ? currentEPin : null;
    const validationError = validateEPinSave(currentPassword, currentEPinValue, newEPin);
    if (validationError) {
      setError(validationError);
      return;
    }

    setIsSubmitting(true);
    try {
      await saveEPin(authSession, currentPassword, currentEPinValue, newEPin);
      onSaved();
    } catch (changeError) {
      setError(changeError instanceof Error ? changeError.message : 'Unable to change your E-PIN.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-[80] flex items-center justify-center px-5 py-8">
      <button
        type="button"
        aria-label="Close Change E-PIN popup"
        className="absolute inset-0 bg-black/65 backdrop-blur-sm"
        onClick={isSubmitting ? undefined : onClose}
      />
      <dialog
        open
        aria-labelledby="change-epin-title"
        className="relative w-full max-w-[440px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
      >
        <button
          type="button"
          onClick={onClose}
          disabled={isSubmitting}
          className="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--text-strong))] disabled:opacity-50"
          aria-label="Close popup"
        >
          <X size={17} strokeWidth={1.8} />
        </button>
        <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <KeyRound size={20} strokeWidth={1.8} />
        </div>
        <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">Security Check</p>
        <h2 id="change-epin-title" className="mt-3 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">
          {actionLabel}
        </h2>
        <p className="mt-3 text-sm leading-6 text-[rgb(var(--text-muted))]">
          {description}
        </p>

        <form className="mt-7 space-y-5" onSubmit={handleSubmit}>
          <label className="block">
            <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Current Password</span>
            <input
              name="currentPassword"
              type="password"
              autoComplete="current-password"
              className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]"
              required
              autoFocus
            />
          </label>
          {isSet && (
            <SecureEPinField
              label="Current E-PIN"
              name="currentEPin"
              value={currentEPin}
              isVisible={showCurrentEPin}
              onChange={setCurrentEPin}
              onToggleVisibility={() => setShowCurrentEPin((visible) => !visible)}
            />
          )}
          <SecureEPinField
            label="New E-PIN"
            name="newEPin"
            value={newEPin}
            isVisible={showNewEPin}
            onChange={setNewEPin}
            onToggleVisibility={() => setShowNewEPin((visible) => !visible)}
          />
          {error && (
            <p className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm font-bold text-red-500" role="alert">
              {error}
            </p>
          )}
          <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="rounded-md border border-[rgb(var(--button-line))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-md bg-[rgb(var(--gold))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold disabled:cursor-not-allowed disabled:opacity-70"
            >
              {submitLabel}
            </button>
          </div>
        </form>
      </dialog>
    </div>
  );
}

function OneTimeEPinNotice({
  ePin,
  onClose,
}: Readonly<{
  ePin: string;
  onClose: () => void;
}>) {
  return (
    <div className="fixed inset-0 z-[90] flex items-center justify-center px-5 py-8">
      <div className="absolute inset-0 bg-black/70 backdrop-blur-sm" />
      <dialog
        open
        aria-labelledby="one-time-epin-title"
        className="relative w-full max-w-[440px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 text-center shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
      >
        <div className="mx-auto grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <KeyRound size={20} strokeWidth={1.8} />
        </div>
        <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">Shown Once</p>
        <h2 id="one-time-epin-title" className="mt-3 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">
          Save Your E-PIN
        </h2>
        <p className="mt-3 text-sm leading-6 text-[rgb(var(--text-muted))]">
          This automatically generated E-PIN cannot be recovered or displayed again.
        </p>
        <output className="mt-7 block rounded-md border border-[rgb(var(--gold))]/35 bg-[rgb(var(--page-bg))] px-5 py-4 font-mono text-2xl font-bold tracking-[0.28em] text-[rgb(var(--text-strong))]">
          {ePin}
        </output>
        <button
          type="button"
          onClick={onClose}
          className="mt-7 w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold"
        >
          I Have Saved It
        </button>
      </dialog>
    </div>
  );
}

function UserPage({
  authSession,
  showHome,
  showAccounts,
}: Readonly<{
  authSession: AuthSession;
  showHome: () => void;
  showAccounts: () => void;
}>) {
  const { user } = authSession;
  const [isEPinSet, setIsEPinSet] = React.useState<boolean | null>(null);
  const [ePinError, setEPinError] = React.useState('');
  const [ePinSuccess, setEPinSuccess] = React.useState('');
  const [isChangeEPinOpen, setIsChangeEPinOpen] = React.useState(false);
  const initials = `${user.firstName.charAt(0)}${user.lastName.charAt(0)}`.toUpperCase();
  const userDetails = [
    ['First name', user.firstName],
    ['Last name', user.lastName],
    ['Email address', user.email],
    ['Date of birth', formatProfileDate(user.dateOfBirth, 'Not provided')],
    ['User role', user.role === 'ADMIN' ? 'Administrator' : 'Client'],
    ['Client since', formatProfileDate(user.createdAt, 'Not available')],
  ];

  React.useEffect(() => {
    let isActive = true;
    setIsEPinSet(null);
    setEPinError('');

    fetchEPinStatus(authSession)
      .then((value) => {
        if (isActive) setIsEPinSet(value);
      })
      .catch((loadError) => {
        if (isActive) {
          setEPinError(loadError instanceof Error ? loadError.message : 'Unable to load E-PIN status.');
        }
      });

    return () => {
      isActive = false;
    };
  }, [authSession]);

  function handleEPinSaved() {
    const successMessage = isEPinSet
      ? 'Your E-PIN was changed successfully.'
      : 'Your E-PIN was set successfully.';
    setIsEPinSet(true);
    setEPinError('');
    setEPinSuccess(successMessage);
    setIsChangeEPinOpen(false);
  }

  let ePinDisplay = 'Checking...';
  if (isEPinSet === true) ePinDisplay = '••••••';
  if (isEPinSet === false) ePinDisplay = 'Not set';

  return (
    <section className="pattern-bg min-h-screen px-6 pb-20 pt-32 sm:px-10 lg:pt-36">
      <div className="mx-auto max-w-[980px]">
        <button
          type="button"
          onClick={showHome}
          className="inline-flex items-center gap-2 text-sm font-bold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
        >
          <ArrowLeft size={16} strokeWidth={1.8} />
          Back to overview
        </button>

        <div className="mt-10 flex flex-col justify-between gap-8 border-b border-[rgb(var(--line))] pb-10 md:flex-row md:items-end">
          <div>
            <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Client Profile</p>
            <h1 className="mt-4 font-display text-[clamp(3rem,5vw,4.5rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
              User Information
            </h1>
            <p className="mt-5 max-w-[610px] text-base leading-7 text-[rgb(var(--text-muted))]">
              Personal and security information associated with your SAFE Bank access.
            </p>
          </div>
          <button
            type="button"
            onClick={showAccounts}
            className="inline-flex items-center justify-center gap-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
          >
            <Wallet size={17} strokeWidth={1.8} />
            Manage accounts
          </button>
        </div>

        <div className="mt-10 grid gap-8 lg:grid-cols-[280px_1fr]">
          <aside className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 text-center">
            <div className="mx-auto grid h-20 w-20 place-items-center rounded-full border border-[rgb(var(--gold))]/40 bg-[rgb(var(--icon-bg))] font-display text-3xl font-bold text-[rgb(var(--gold))]">
              {initials}
            </div>
            <h2 className="mt-5 font-display text-2xl font-semibold text-[rgb(var(--text-strong))]">
              {user.firstName} {user.lastName}
            </h2>
            <p className="mt-2 break-all text-sm font-semibold text-[rgb(var(--text-muted))]">{user.email}</p>
            <div className="mt-6 border-t border-[rgb(var(--line))] pt-5">
              <div className="inline-flex items-center gap-2 text-xs font-extrabold uppercase tracking-[0.2em] text-emerald-500">
                <ShieldCheck size={15} strokeWidth={1.9} />
                Verified user
              </div>
            </div>
          </aside>

          <div className="space-y-8">
            <section className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault sm:p-8">
              <div className="flex items-center gap-3 border-b border-[rgb(var(--line))] pb-5">
                <UserRound size={19} strokeWidth={1.8} className="text-[rgb(var(--gold))]" />
                <h2 className="font-display text-2xl font-semibold text-[rgb(var(--text-strong))]">Personal details</h2>
              </div>
              <dl className="mt-2 divide-y divide-[rgb(var(--line))]">
                {userDetails.map(([label, value]) => (
                  <div key={label} className="grid gap-2 py-4 sm:grid-cols-[170px_1fr] sm:items-center">
                    <dt className="text-[0.62rem] font-extrabold uppercase tracking-[0.2em] text-[rgb(var(--text-muted))]">{label}</dt>
                    <dd className="break-words text-sm font-bold text-[rgb(var(--text-strong))]">{value}</dd>
                  </div>
                ))}
              </dl>
            </section>

            <section className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 sm:p-8">
              <div className="flex items-center gap-3 border-b border-[rgb(var(--line))] pb-5">
                <KeyRound size={19} strokeWidth={1.8} className="text-[rgb(var(--gold))]" />
                <h2 className="font-display text-2xl font-semibold text-[rgb(var(--text-strong))]">Password</h2>
              </div>
              <div className="mt-6">
                <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.2em] text-[rgb(var(--text-muted))]">Current password</p>
                <div className="mt-3 flex items-center gap-3 rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3">
                  <LockKeyhole size={17} strokeWidth={1.8} className="shrink-0 text-[rgb(var(--text-muted))]" />
                  <span className="font-mono text-base tracking-[0.2em] text-[rgb(var(--text-strong))]" aria-label="Password hidden">
                    ••••••••••••
                  </span>
                </div>
                <p className="mt-3 text-xs font-semibold leading-5 text-[rgb(var(--text-muted))]">
                  Your password is hidden and is never returned by the server.
                </p>
              </div>
              <div className="mt-7 border-t border-[rgb(var(--line))] pt-6">
                <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.2em] text-[rgb(var(--text-muted))]">E-PIN</p>
                <div className="mt-3 flex items-center gap-3 rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3">
                  <KeyRound size={17} strokeWidth={1.8} className="shrink-0 text-[rgb(var(--text-muted))]" />
                  <span className="min-w-0 flex-1 font-mono text-base tracking-[0.2em] text-[rgb(var(--text-strong))]">
                    {ePinDisplay}
                  </span>
                </div>
                <p className="mt-3 text-xs font-semibold leading-5 text-[rgb(var(--text-muted))]">
                  Your E-PIN is never returned by the server.
                </p>
                {ePinError && <p className="mt-3 text-sm font-bold text-red-500" role="alert">{ePinError}</p>}
                {ePinSuccess && <output className="mt-3 block text-sm font-bold text-emerald-500">{ePinSuccess}</output>}
                <button
                  type="button"
                  onClick={() => {
                    setEPinSuccess('');
                    setIsChangeEPinOpen(true);
                  }}
                  disabled={isEPinSet === null || Boolean(ePinError)}
                  className="mt-5 rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
                >
                  {isEPinSet ? 'Change E-PIN' : 'Set E-PIN'}
                </button>
              </div>
            </section>
          </div>
        </div>
      </div>
      {isChangeEPinOpen && (
        <ChangeEPinModal
          authSession={authSession}
          isSet={Boolean(isEPinSet)}
          onClose={() => setIsChangeEPinOpen(false)}
          onSaved={handleEPinSaved}
        />
      )}
    </section>
  );
}

const EMPTY_TRANSFER_DRAFT: TransferDraft = {
  sourceAccountIban: '',
  destinationAccountIban: '',
  amount: '',
  currency: 'EUR',
  reason: '',
};

type TransactionsPageProps = Readonly<{
  authSession: AuthSession;
  showHome: () => void;
}>;

function getTransferValidationErrors(
  draft: TransferDraft,
  accounts: ClientAccount[],
  recipientAccount: RecipientAccountResponse | null,
): TransferValidationErrors {
  const validationErrors: TransferValidationErrors = {};
  const amount = Number(draft.amount);
  const sourceAccount = accounts.find((account) => account.iban === draft.sourceAccountIban) ?? null;

  if (!draft.sourceAccountIban) {
    validationErrors.sourceAccountIban = 'Choose a source account.';
  } else if (!sourceAccount) {
    validationErrors.sourceAccountIban = 'Choose one of your available accounts.';
  }

  if (!draft.destinationAccountIban.trim()) {
    validationErrors.destinationAccountIban = 'Recipient IBAN is required.';
  } else if (recipientAccount?.iban !== draft.destinationAccountIban.trim().toUpperCase()) {
    validationErrors.destinationAccountIban = 'Enter a valid recipient IBAN from SAFE Bank.';
  }

  if (!draft.amount.trim()) {
    validationErrors.amount = 'Amount is required.';
  } else if (!Number.isFinite(amount) || amount <= 0) {
    validationErrors.amount = 'Amount must be greater than zero.';
  } else if (canCheckTransferBalance(sourceAccount, recipientAccount) && amount > sourceAccount.balance) {
    validationErrors.amount = 'Insufficient funds in the selected account.';
  }

  if (!draft.reason.trim()) {
    validationErrors.reason = 'Reason is required.';
  }

  return validationErrors;
}

function canCheckTransferBalance(
  sourceAccount: ClientAccount | null,
  recipientAccount: RecipientAccountResponse | null,
): sourceAccount is ClientAccount {
  return Boolean(sourceAccount?.currency && sourceAccount.currency === recipientAccount?.currency);
}

function getTransferAmountLabel(draft: TransferDraft) {
  if (!draft.amount) return formatCurrencyAmount(0, draft.currency);
  return formatCurrencyAmount(Number(draft.amount), draft.currency);
}

function getDailyLimitLabel(sourceAccount: ClientAccount | null) {
  return formatCurrencyAmount(500000, sourceAccount?.currency ?? 'EUR');
}

function getCurrencyCheckLabel(
  recipientAccount: RecipientAccountResponse | null,
  canCheckSourceBalance: boolean,
) {
  if (!recipientAccount) return '';
  let balanceMessage = ' · Balance check skipped for different currencies';
  if (canCheckSourceBalance) {
    balanceMessage = '';
  }
  return `Recipient currency: ${recipientAccount.currency}${balanceMessage}`;
}

function getTransferSummaryRows(
  draft: TransferDraft,
  sourceAccount: ClientAccount | null,
) {
  let sourceAccountLabel = 'Not selected';
  if (sourceAccount) {
    sourceAccountLabel = `${sourceAccount.name} · ${maskIban(sourceAccount.iban)}`;
  }

  return [
    ['From', sourceAccountLabel],
    ['Recipient', draft.destinationAccountIban],
    ['Amount', getTransferAmountLabel(draft)],
    ['Currency', draft.currency],
    ['Reason', draft.reason],
  ];
}

function useTransferAccounts(
  authSession: AuthSession,
  setDraft: React.Dispatch<React.SetStateAction<TransferDraft>>,
) {
  const [accounts, setAccounts] = React.useState<ClientAccount[]>([]);
  const [isLoadingAccounts, setIsLoadingAccounts] = React.useState(true);
  const [accountsError, setAccountsError] = React.useState('');

  const loadTransferAccounts = React.useCallback(async () => {
    setIsLoadingAccounts(true);
    setAccountsError('');
    try {
      const loadedAccounts = await fetchAccounts(authSession);
      setAccounts(loadedAccounts);
      setDraft((currentDraft) => {
        if (currentDraft.sourceAccountIban || loadedAccounts.length === 0) return currentDraft;
        const firstAccount = loadedAccounts[0];
        return {
          ...currentDraft,
          sourceAccountIban: firstAccount.iban,
          currency: firstAccount.currency,
        };
      });
    } catch (error) {
      setAccountsError(error instanceof Error ? error.message : 'Unable to load accounts.');
    } finally {
      setIsLoadingAccounts(false);
    }
  }, [authSession, setDraft]);

  React.useEffect(() => {
    loadTransferAccounts();
  }, [loadTransferAccounts]);

  return {
    accounts,
    accountsError,
    isLoadingAccounts,
    loadTransferAccounts,
  };
}

function useTransactionHistory(authSession: AuthSession) {
  const [transactions, setTransactions] = React.useState<TransactionHistoryResponse[]>([]);
  const [isLoadingTransactions, setIsLoadingTransactions] = React.useState(true);
  const [transactionsError, setTransactionsError] = React.useState('');

  const loadTransactions = React.useCallback(async () => {
    setIsLoadingTransactions(true);
    setTransactionsError('');
    try {
      setTransactions(await fetchTransactions(authSession));
    } catch (error) {
      setTransactionsError(error instanceof Error ? error.message : 'Unable to load transactions.');
    } finally {
      setIsLoadingTransactions(false);
    }
  }, [authSession]);

  React.useEffect(() => {
    loadTransactions();
  }, [loadTransactions]);

  return {
    transactions,
    isLoadingTransactions,
    transactionsError,
    loadTransactions,
  };
}

function getTransferApiFieldErrors(fieldErrors: Record<string, string>): TransferValidationErrors {
  const validationErrors: TransferValidationErrors = {};
  const transferFields: (keyof TransferDraft)[] = [
    'sourceAccountIban',
    'destinationAccountIban',
    'amount',
    'reason',
  ];

  transferFields.forEach((field) => {
    if (fieldErrors[field]) validationErrors[field] = fieldErrors[field];
  });
  return validationErrors;
}

function getTransferSuccessMessage(transfer: TransferResponse) {
  if (transfer.status === 'PENDING') {
    return `Transfer #${transfer.transactionId} was submitted and is pending.`;
  }
  return `Transfer #${transfer.transactionId} completed successfully.`;
}

function formatTransactionDate(timestamp: string) {
  const date = new Date(timestamp);
  if (Number.isNaN(date.getTime())) return 'Date unavailable';
  return new Intl.DateTimeFormat('en-US', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

function getTransactionStatusClass(status: TransactionStatus) {
  if (status === 'COMPLETED') return 'bg-emerald-500/15 text-emerald-500';
  if (status === 'FAILED') return 'bg-red-500/15 text-red-500';
  return 'bg-[rgb(var(--gold))]/15 text-[rgb(var(--gold))]';
}

function getTransactionDisplay(transaction: TransactionHistoryResponse, accounts: ClientAccount[]) {
  const sourceAccount = accounts.find((account) => account.iban === transaction.sourceIban) ?? null;
  const destinationAccount = accounts.find((account) => account.iban === transaction.destinationIban) ?? null;
  const isOutgoing = Boolean(sourceAccount);
  const counterpartyIban = isOutgoing ? transaction.destinationIban : transaction.sourceIban;
  const currency = isOutgoing
    ? transaction.sourceCurrency ?? sourceAccount?.currency ?? 'EUR'
    : transaction.destinationCurrency ?? destinationAccount?.currency ?? transaction.sourceCurrency ?? 'EUR';
  const displayedAmount = isOutgoing
    ? transaction.amount
    : transaction.creditedAmount ?? transaction.amount;
  const amount = Number(displayedAmount);
  const signedAmount = isOutgoing ? -amount : amount;

  return {
    counterpartyIban,
    amountLabel: `${signedAmount > 0 ? '+' : ''}${formatCurrencyAmount(signedAmount, currency)}`,
    isIncoming: signedAmount > 0,
    direction: isOutgoing ? 'Sent' : 'Received',
  };
}

function TransactionsPage({ authSession, showHome }: TransactionsPageProps) {
  const [draft, setDraft] = React.useState<TransferDraft>(EMPTY_TRANSFER_DRAFT);
  const [fieldErrors, setFieldErrors] = React.useState<TransferValidationErrors>({});
  const {
    accounts,
    accountsError,
    isLoadingAccounts,
    loadTransferAccounts,
  } = useTransferAccounts(authSession, setDraft);
  const {
    transactions,
    isLoadingTransactions,
    transactionsError,
    loadTransactions,
  } = useTransactionHistory(authSession);
  const [recipientAccount, setRecipientAccount] = React.useState<RecipientAccountResponse | null>(null);
  const [recipientLookupError, setRecipientLookupError] = React.useState('');
  const [isCheckingRecipient, setIsCheckingRecipient] = React.useState(false);
  const [activeStep, setActiveStep] = React.useState<'summary' | 'epin' | null>(null);
  const [ePin, setEPin] = React.useState('');
  const [ePinError, setEPinError] = React.useState('');
  const [transferError, setTransferError] = React.useState('');
  const [transferNotice, setTransferNotice] = React.useState('');
  const [isSubmittingTransfer, setIsSubmittingTransfer] = React.useState(false);

  const sourceAccount = accounts.find((account) => account.iban === draft.sourceAccountIban) ?? null;
  const totalBalanceLabel = getPortfolioTotalLabel(accounts);
  const dailyLimitLabel = getDailyLimitLabel(sourceAccount);
  const canCheckSourceBalance = canCheckTransferBalance(sourceAccount, recipientAccount);
  const currencyCheckLabel = getCurrencyCheckLabel(recipientAccount, canCheckSourceBalance);

  function updateDraft(field: keyof TransferDraft, value: string) {
    setTransferNotice('');
    setTransferError('');
    if (field === 'destinationAccountIban') {
      setRecipientAccount(null);
      setRecipientLookupError('');
    }
    setFieldErrors((currentErrors) => {
      const nextErrors = { ...currentErrors };
      delete nextErrors[field];
      return nextErrors;
    });
    setDraft((currentDraft) => {
      if (field !== 'sourceAccountIban') {
        return { ...currentDraft, [field]: value };
      }
      const nextAccount = accounts.find((account) => account.iban === value);
      return {
        ...currentDraft,
        sourceAccountIban: value,
        currency: nextAccount?.currency ?? currentDraft.currency,
      };
    });
  }

  async function checkRecipientIban(iban = draft.destinationAccountIban): Promise<RecipientAccountResponse | null> {
    const normalizedIban = iban.trim().toUpperCase();
    setRecipientLookupError('');
    setRecipientAccount(null);

    if (!normalizedIban) {
      return null;
    }

    setIsCheckingRecipient(true);
    try {
      const account = await lookupRecipientAccount(authSession, normalizedIban);
      setRecipientAccount(account);
      setDraft((currentDraft) => ({
        ...currentDraft,
        destinationAccountIban: account.iban,
      }));
      return account;
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Recipient IBAN was not found.';
      setRecipientLookupError(message);
      return null;
    } finally {
      setIsCheckingRecipient(false);
    }
  }

  async function reviewTransfer(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setTransferNotice('');
    setTransferError('');
    setEPinError('');
    const normalizedDestinationIban = draft.destinationAccountIban.trim().toUpperCase();
    const verifiedRecipient = recipientAccount?.iban === normalizedDestinationIban
      ? recipientAccount
      : await checkRecipientIban(normalizedDestinationIban);
    const validationErrors = getTransferValidationErrors(draft, accounts, verifiedRecipient);
    setFieldErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    try {
      const isEPinSet = await fetchEPinStatus(authSession);
      if (!isEPinSet) {
        setEPinError('Set your E-PIN from the profile page before sending transfers.');
        setActiveStep('epin');
        return;
      }
      setActiveStep('summary');
    } catch (error) {
      setEPinError(error instanceof Error ? error.message : 'Unable to check E-PIN status.');
      setActiveStep('epin');
    }
  }

  async function verifyTransferEPin(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setEPinError('');
    if (!EPIN_PATTERN.test(ePin)) {
      setEPinError(EPIN_VALIDATION_MESSAGE);
      return;
    }

    setIsSubmittingTransfer(true);
    let isEPinVerified = false;
    try {
      await verifyEPin(authSession, ePin);
      isEPinVerified = true;
      const transfer = await submitTransfer(authSession, draft);
      setActiveStep(null);
      setEPin('');
      setRecipientAccount(null);
      setRecipientLookupError('');
      await Promise.all([loadTransferAccounts(), loadTransactions()]);

      if (transfer.status === 'FAILED') {
        setTransferError(`Transfer #${transfer.transactionId} failed. Check the available balance and try again.`);
        return;
      }

      setDraft({
        ...EMPTY_TRANSFER_DRAFT,
        sourceAccountIban: draft.sourceAccountIban,
        currency: sourceAccount?.currency ?? 'EUR',
      });
      setTransferNotice(getTransferSuccessMessage(transfer));
    } catch (error) {
      if (isEPinVerified) {
        if (error instanceof ApiRequestError) {
          setFieldErrors(getTransferApiFieldErrors(error.fieldErrors));
        }
        setActiveStep(null);
        setEPin('');
        setTransferError(error instanceof Error ? error.message : 'Unable to send the transfer.');
      } else {
        setEPinError(error instanceof Error ? error.message : 'Unable to verify E-PIN.');
      }
    } finally {
      setIsSubmittingTransfer(false);
    }
  }

  function closeTransferModal() {
    setActiveStep(null);
    setEPin('');
    setEPinError('');
  }

  function retryTransferAccounts() {
    loadTransferAccounts();
  }

  function retryTransactions() {
    loadTransactions();
  }

  function checkCurrentRecipientIban() {
    checkRecipientIban();
  }

  const summaryRows = getTransferSummaryRows(draft, sourceAccount);

  return (
    <section className="pattern-bg min-h-screen px-6 pb-20 pt-32 sm:px-10 lg:pt-36">
      <div className="mx-auto max-w-[980px]">
        <button
          type="button"
          onClick={showHome}
          className="text-sm font-bold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
        >
          Back to overview
        </button>

        <div className="mt-10 grid gap-10 lg:grid-cols-[0.95fr_1.05fr]">
          <div>
            <Eyebrow>Secure Transfer Desk</Eyebrow>
            <h1 className="mt-8 font-display text-[clamp(3rem,4vw,4.5rem)] font-semibold leading-[1.02] text-[rgb(var(--text-strong))]">
              Transactions
              <br />
              & Transfers
            </h1>
            <p className="mt-7 max-w-[480px] text-lg leading-8 text-[rgb(var(--text-muted))]">
              Move funds between accounts, initiate wire transfers, and review recent banking activity from one private workspace.
            </p>

            <div className="mt-10 grid gap-4 sm:grid-cols-3 lg:grid-cols-1">
              {[
                ['Available Balance', totalBalanceLabel],
                ['Accounts', String(accounts.length)],
                ['Daily Limit', dailyLimitLabel],
              ].map(([label, value]) => (
                <div key={label} className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-5">
                  <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.26em] text-[rgb(var(--text-muted))]">{label}</p>
                  <p className="mt-3 font-display text-2xl font-bold text-[rgb(var(--text-strong))]">{value}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="flex min-h-[690px] flex-col rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault">
            <div className="flex items-center justify-between border-b border-[rgb(var(--line))] pb-5">
              <div>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">New Transfer</p>
                <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Send Funds</h2>
              </div>
              <div className="grid h-11 w-11 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
                <Zap size={18} strokeWidth={1.8} />
              </div>
              </div>

            {accountsError && (
              <div className="mt-5 rounded-md border border-red-500/30 bg-red-500/10 p-4">
                <p className="text-sm font-bold text-red-500" role="alert">{accountsError}</p>
                <button
                  type="button"
                  onClick={retryTransferAccounts}
                  className="mt-3 rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
                >
                  Retry
                </button>
              </div>
            )}

            <form className="mt-6 flex flex-1 flex-col gap-4" onSubmit={reviewTransfer}>
              <label>
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">From Account</span>
                <select
                  value={draft.sourceAccountIban}
                  onChange={(event) => updateDraft('sourceAccountIban', event.target.value)}
                  disabled={isLoadingAccounts || accounts.length === 0}
                  className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60 ${
                    fieldErrors.sourceAccountIban ? 'border-red-500' : 'border-[rgb(var(--line))]'
                  }`}
                >
                  <option value="">{isLoadingAccounts ? 'Loading accounts...' : 'Choose account'}</option>
                  {accounts.map((account) => (
                    <option key={account.iban} value={account.iban}>
                      {account.name} · {maskIban(account.iban)} · {formatAccountBalance(account)}
                    </option>
                  ))}
                </select>
                {fieldErrors.sourceAccountIban && <p className="mt-2 text-xs font-bold text-red-500">{fieldErrors.sourceAccountIban}</p>}
              </label>
              <label>
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Recipient</span>
                <input
                  value={draft.destinationAccountIban}
                  onChange={(event) => updateDraft('destinationAccountIban', event.target.value.toUpperCase())}
                  onBlur={checkCurrentRecipientIban}
                  className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
                    fieldErrors.destinationAccountIban || recipientLookupError ? 'border-red-500' : 'border-[rgb(var(--line))]'
                  }`}
                  placeholder="IBAN"
                />
                {isCheckingRecipient && <p className="mt-2 text-xs font-bold text-[rgb(var(--text-muted))]">Checking recipient IBAN...</p>}
                {recipientAccount && !isCheckingRecipient && <p className="mt-2 text-xs font-bold text-emerald-500">{currencyCheckLabel}</p>}
                {(fieldErrors.destinationAccountIban || recipientLookupError) && (
                  <p className="mt-2 text-xs font-bold text-red-500">{fieldErrors.destinationAccountIban ?? recipientLookupError}</p>
                )}
              </label>
              <div className="grid gap-4 sm:grid-cols-2">
                <label>
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Amount</span>
                  <input
                    value={draft.amount}
                    onChange={(event) => updateDraft('amount', event.target.value)}
                    inputMode="decimal"
                    className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
                      fieldErrors.amount ? 'border-red-500' : 'border-[rgb(var(--line))]'
                    }`}
                    placeholder="0.00"
                  />
                  {fieldErrors.amount && <p className="mt-2 text-xs font-bold text-red-500">{fieldErrors.amount}</p>}
                </label>
                <label>
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Currency</span>
                  <input
                    value={draft.currency}
                    readOnly
                    className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-muted))] outline-none"
                  />
                  <p className="mt-2 text-xs font-bold text-[rgb(var(--text-muted))]">
                    Set automatically from the source account.
                  </p>
                </label>
              </div>
              <label className="flex flex-1 flex-col">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Reason</span>
                <textarea
                  value={draft.reason}
                  onChange={(event) => updateDraft('reason', event.target.value)}
                  className={`min-h-[150px] flex-1 resize-none rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold leading-6 text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
                    fieldErrors.reason ? 'border-red-500' : 'border-[rgb(var(--line))]'
                  }`}
                  placeholder="Payment reference or transfer purpose"
                />
                {fieldErrors.reason && <p className="mt-2 text-xs font-bold text-red-500">{fieldErrors.reason}</p>}
              </label>
              {ePinError && !activeStep && <p className="text-sm font-bold text-red-500" role="alert">{ePinError}</p>}
              {transferError && <p className="text-sm font-bold text-red-500" role="alert">{transferError}</p>}
              {transferNotice && <output className="text-sm font-bold text-emerald-500">{transferNotice}</output>}
              <button
                className="mt-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:translate-y-0"
                type="submit"
                disabled={isLoadingAccounts || isCheckingRecipient || accounts.length === 0}
              >
                {isCheckingRecipient ? 'Checking Recipient...' : 'Review Transfer'}
              </button>
            </form>
          </div>
        </div>

        <div className="mt-12 rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6">
          <div className="mb-6 flex flex-col justify-between gap-3 border-b border-[rgb(var(--line))] pb-5 sm:flex-row sm:items-center">
            <div>
              <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Recent Activity</p>
              <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Transaction History</h2>
            </div>
            <span className="text-sm font-bold text-[rgb(var(--text-muted))]">Latest transactions</span>
          </div>
          {isLoadingTransactions && (
            <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-5 text-sm font-bold text-[rgb(var(--text-muted))]">
              Loading transactions...
            </p>
          )}
          {!isLoadingTransactions && transactionsError && (
            <div className="rounded-md border border-red-500/30 bg-red-500/10 p-4">
              <p className="text-sm font-bold text-red-500" role="alert">{transactionsError}</p>
              <button
                type="button"
                onClick={retryTransactions}
                className="mt-3 rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
              >
                Retry
              </button>
            </div>
          )}
          {!isLoadingTransactions && !transactionsError && transactions.length === 0 && (
            <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-5 text-sm font-bold text-[rgb(var(--text-muted))]">
              No transactions yet. Your transfers will appear here.
            </p>
          )}
          {!isLoadingTransactions && !transactionsError && transactions.length > 0 && (
            <div className="divide-y divide-[rgb(var(--line))]">
              {transactions.map((transaction) => {
                const display = getTransactionDisplay(transaction, accounts);
                return (
                  <div key={transaction.transactionId} className="grid gap-3 py-4 sm:grid-cols-[1fr_auto_auto] sm:items-center">
                    <div className="min-w-0">
                      <p className="break-all font-mono text-sm font-bold text-[rgb(var(--text-strong))]">{display.counterpartyIban}</p>
                      <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">
                        {display.direction} · {transaction.reason} · {formatTransactionDate(transaction.timestamp)}
                      </p>
                    </div>
                    <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.08em] ${getTransactionStatusClass(transaction.status)}`}>
                      {transaction.status}
                    </span>
                    <span className={`font-display text-xl font-bold ${display.isIncoming ? 'text-emerald-500' : 'text-[rgb(var(--text-strong))]'}`}>
                      {display.amountLabel}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
      {activeStep && (
        <dialog open className="fixed inset-0 z-[80] grid h-full w-full place-items-center overflow-y-auto bg-black/65 px-5 py-8 backdrop:bg-transparent">
          <div className="relative w-full max-w-[560px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)]">
            <button
              type="button"
              onClick={closeTransferModal}
              className="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--text-strong))]"
              aria-label="Close transfer popup"
            >
              <X size={17} strokeWidth={1.8} />
            </button>

            {activeStep === 'summary' && (
              <div>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Transfer Summary</p>
                <h2 className="mt-3 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Review Details</h2>
                <dl className="mt-7 divide-y divide-[rgb(var(--line))] rounded-md border border-[rgb(var(--line))]">
                  {summaryRows.map(([label, value]) => (
                    <div key={label} className="grid gap-2 px-4 py-4 sm:grid-cols-[130px_1fr]">
                      <dt className="text-xs font-extrabold uppercase tracking-[0.2em] text-[rgb(var(--text-muted))]">{label}</dt>
                      <dd className="break-words text-sm font-bold text-[rgb(var(--text-strong))]">{value}</dd>
                    </div>
                  ))}
                </dl>
                <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                  <button
                    type="button"
                    onClick={() => setActiveStep('epin')}
                    className="rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold"
                  >
                    Confirm & Continue
                  </button>
                  <button
                    type="button"
                    onClick={closeTransferModal}
                    className="rounded-md border border-[rgb(var(--button-line))] px-6 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
                  >
                    Back
                  </button>
                </div>
              </div>
            )}

            {activeStep === 'epin' && (
              <form onSubmit={verifyTransferEPin}>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">E-PIN Verification</p>
                <h2 className="mt-3 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Confirm Transfer</h2>
                <p className="mt-4 text-sm leading-6 text-[rgb(var(--text-muted))]">
                  Enter your 6-digit E-PIN to authorize and send this transfer.
                </p>
                <label className="mt-6 block">
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">E-PIN</span>
                  <input
                    value={ePin}
                    onChange={(event) => {
                      setEPin(sanitizeEPin(event.target.value));
                      setEPinError('');
                    }}
                    type="password"
                    inputMode="numeric"
                    autoComplete="one-time-code"
                    pattern={EPIN_INPUT_PATTERN}
                    maxLength={EPIN_LENGTH}
                    className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-center font-mono text-lg font-bold tracking-[0.4em] text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))] ${
                      ePinError ? 'border-red-500' : 'border-[rgb(var(--line))]'
                    }`}
                    placeholder="••••••"
                  />
                </label>
                {ePinError && <p className="mt-3 text-sm font-bold text-red-500" role="alert">{ePinError}</p>}
                <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                  <button
                    type="submit"
                    disabled={isSubmittingTransfer}
                    className="rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {isSubmittingTransfer ? 'Sending...' : 'Confirm & Send'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setActiveStep('summary')}
                    disabled={isSubmittingTransfer}
                    className="rounded-md border border-[rgb(var(--button-line))] px-6 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    Back
                  </button>
                </div>
              </form>
            )}
          </div>
        </dialog>
      )}
    </section>
  );
}

type PortfolioPageProps = Readonly<{
  authSession: AuthSession;
  showHome: () => void;
  showTransactions: () => void;
}>;

function PortfolioPage({
  authSession,
  showHome,
  showTransactions,
}: PortfolioPageProps) {
  const [accounts, setAccounts] = React.useState<ClientAccount[]>([]);
  const [portfolioError, setPortfolioError] = React.useState('');
  const [isLoadingPortfolio, setIsLoadingPortfolio] = React.useState(true);
  const [topUpAccount, setTopUpAccount] = React.useState<ClientAccount | null>(null);

  const loadPortfolio = React.useCallback(async () => {
    setIsLoadingPortfolio(true);
    setPortfolioError('');
    try {
      setAccounts(await fetchAccounts(authSession));
    } catch (error) {
      setPortfolioError(error instanceof Error ? error.message : 'Unable to load portfolio.');
    } finally {
      setIsLoadingPortfolio(false);
    }
  }, [authSession]);

  React.useEffect(() => {
    loadPortfolio();
  }, [loadPortfolio]);

  const holdings = getPortfolioHoldings(accounts);
  const allocations = getPortfolioCurrencyAllocations(accounts);
  const portfolioMetrics = [
    ['Total Value', getPortfolioTotalLabel(accounts)],
    ['Accounts', String(accounts.length)],
    ['Risk Profile', getPortfolioRiskProfile(accounts)],
  ];
  let allocationContent: React.ReactNode;
  if (isLoadingPortfolio) {
    allocationContent = <p className="text-sm font-bold text-[rgb(var(--text-muted))]">Loading portfolio...</p>;
  } else if (allocations.length > 0) {
    allocationContent = allocations.map((holding) => (
      <div key={holding.category}>
        <div className="mb-2 flex items-center justify-between text-sm font-bold">
          <span className="text-[rgb(var(--text-strong))]">{holding.category}</span>
          <span className="text-[rgb(var(--gold))]">{holding.allocation}</span>
        </div>
        <div className="h-2 rounded-full bg-[rgb(var(--line))]">
          <div className="h-full rounded-full bg-[rgb(var(--gold))]" style={{ width: holding.allocationWidth }} />
        </div>
      </div>
    ));
  } else {
    allocationContent = <p className="text-sm font-bold text-[rgb(var(--text-muted))]">No allocation data yet.</p>;
  }

  let holdingsContent: React.ReactNode;
  if (portfolioError) {
    holdingsContent = (
      <div className="rounded-lg border border-red-500/30 bg-red-500/10 p-5">
        <p className="text-sm font-bold text-red-500" role="alert">{portfolioError}</p>
        <button
          type="button"
          onClick={loadPortfolio}
          className="mt-4 rounded-md border border-[rgb(var(--button-line))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
        >
          Retry
        </button>
      </div>
    );
  } else if (isLoadingPortfolio) {
    holdingsContent = (
      <div className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--page-bg))] p-5 text-sm font-bold text-[rgb(var(--text-muted))]">
        Loading holdings...
      </div>
    );
  } else if (holdings.length > 0) {
    holdingsContent = (
      <div className="divide-y divide-[rgb(var(--line))]">
        {holdings.map((holding) => (
          <div key={holding.account.id} className="grid gap-3 py-5 sm:grid-cols-[1.1fr_0.7fr_auto_auto] sm:items-center">
            <div>
              <p className="font-bold text-[rgb(var(--text-strong))]">{holding.name}</p>
              <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{holding.category}</p>
            </div>
            <span className="text-sm font-extrabold text-[rgb(var(--text-muted))]">{holding.allocation} allocation</span>
            <span className="font-display text-xl font-bold text-[rgb(var(--text-strong))]">{holding.value}</span>
            <div className="flex flex-wrap items-center gap-3 sm:justify-end">
              <span className="text-sm font-extrabold text-emerald-500">{holding.status}</span>
              <button
                type="button"
                onClick={() => setTopUpAccount(holding.account)}
                disabled={holding.account.status !== 'ACTIVE'}
                className="inline-flex items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-45"
              >
                <Plus size={14} strokeWidth={2} />
                Top up
              </button>
            </div>
          </div>
        ))}
      </div>
    );
  } else {
    holdingsContent = (
      <div className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--page-bg))] p-8 text-center">
        <div className="mx-auto grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <ChartPie size={20} strokeWidth={1.8} />
        </div>
        <h3 className="mt-5 font-display text-2xl font-semibold text-[rgb(var(--text-strong))]">No portfolio data yet</h3>
        <p className="mx-auto mt-3 max-w-[420px] text-sm leading-6 text-[rgb(var(--text-muted))]">
          Create an account or fund an existing account to populate your portfolio.
        </p>
      </div>
    );
  }

  return (
    <section className="pattern-bg min-h-screen px-6 pb-20 pt-32 sm:px-10 lg:pt-36">
      <div className="mx-auto max-w-[980px]">
        <button
          type="button"
          onClick={showHome}
          className="inline-flex items-center gap-2 text-sm font-bold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
        >
          <ArrowLeft size={16} strokeWidth={1.8} />
          Back to overview
        </button>

        <div className="mt-10 grid gap-10 lg:grid-cols-[1fr_360px]">
          <div>
            <Eyebrow>Private Portfolio</Eyebrow>
            <h1 className="mt-8 font-display text-[clamp(3rem,4vw,4.5rem)] font-semibold leading-[1.02] text-[rgb(var(--text-strong))]">
              Portfolio
              <br />
              Command Center
            </h1>
            <p className="mt-7 max-w-[560px] text-lg leading-8 text-[rgb(var(--text-muted))]">
              Review your account allocation, balances, and transfer-ready holdings from live banking data.
            </p>
            <div className="mt-10 grid gap-4 sm:grid-cols-3">
              {portfolioMetrics.map(([label, value]) => (
                <div key={label} className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-5">
                  <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.26em] text-[rgb(var(--text-muted))]">{label}</p>
                  <p className="mt-3 font-display text-2xl font-bold text-[rgb(var(--text-strong))]">{value}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault">
            <div className="flex items-center justify-between border-b border-[rgb(var(--line))] pb-5">
              <div>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Allocation</p>
                <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Overview</h2>
              </div>
              <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
                <ChartPie size={21} strokeWidth={1.8} />
              </div>
            </div>
            <div className="mt-6 space-y-5">
              {allocationContent}
            </div>
          </div>
        </div>

        <div className="mt-12 rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6">
          <div className="mb-6 flex flex-col justify-between gap-3 border-b border-[rgb(var(--line))] pb-5 sm:flex-row sm:items-center">
            <div>
              <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Holdings</p>
              <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Managed Mandates</h2>
            </div>
            <button
              type="button"
              onClick={showTransactions}
              className="rounded-md border border-[rgb(var(--button-line))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
            >
              Transfer Funds
            </button>
          </div>
          {holdingsContent}
        </div>
      </div>
      {topUpAccount && (
        <TopUpModal
          account={topUpAccount}
          authSession={authSession}
          onClose={() => setTopUpAccount(null)}
          onPaymentConfirmed={loadPortfolio}
        />
      )}
    </section>
  );
}

type AdminPageProps = Readonly<{
  authSession: AuthSession;
  showHome: () => void;
}>;

type AdminUsersPanelProps = Readonly<{
  searchQuery: string;
  setSearchQuery: React.Dispatch<React.SetStateAction<string>>;
  accountActionError: string;
  isLoadingUsers: boolean;
  usersError: string;
  filteredUsers: AdminUserResponse[];
  updatingAccountIban: string | null;
  loadUsers: () => Promise<void>;
  toggleAdminAccountStatus: (account: AccountResponse) => Promise<void>;
}>;

type AdminUserCardProps = Readonly<{
  user: AdminUserResponse;
  updatingAccountIban: string | null;
  toggleAdminAccountStatus: (account: AccountResponse) => Promise<void>;
}>;

type AdminAccountRowProps = Readonly<{
  account: AccountResponse;
  updatingAccountIban: string | null;
  toggleAdminAccountStatus: (account: AccountResponse) => Promise<void>;
}>;

type AdminTransactionsPanelProps = Readonly<{
  isLoadingTransactions: boolean;
  transactionsError: string;
  transactions: AdminTransactionResponse[];
  loadTransactions: () => Promise<void>;
}>;

type AdminRetryPanelProps = Readonly<{
  message: string;
  onRetry: () => Promise<void>;
}>;

function runAsyncAction(action: () => Promise<void>) {
  action().catch(() => undefined);
}

function getAdminAccountStatusClass(status: AccountStatus) {
  return status === 'BLOCKED' ? 'bg-red-500/15 text-red-500' : 'bg-emerald-500/15 text-emerald-500';
}

function getAdminUserStatusClass(isActive: boolean) {
  return isActive ? 'bg-emerald-500/15 text-emerald-500' : 'bg-red-500/15 text-red-500';
}

function getClientQueuePriorityClass(priority: string) {
  if (priority === 'High') return 'bg-red-500/15 text-red-500';
  if (priority === 'Medium') return 'bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]';
  return 'bg-emerald-500/15 text-emerald-500';
}

function getAdminAccountAction(status: AccountStatus) {
  return status === 'BLOCKED' ? 'unblock' : 'block';
}

function getAdminAccountActionLabel(isUpdating: boolean, action: string) {
  if (isUpdating) return 'Updating...';
  return action === 'block' ? 'Block' : 'Unblock';
}

function updateUsersWithAccount(currentUsers: AdminUserResponse[], updatedAccount: AccountResponse) {
  return currentUsers.map((user) => ({
    ...user,
    accounts: user.accounts.map((candidate) => (
      candidate.iban === updatedAccount.iban ? updatedAccount : candidate
    )),
  }));
}

function AdminRetryPanel({ message, onRetry }: AdminRetryPanelProps) {
  return (
    <div className="rounded-lg border border-red-500/30 bg-red-500/10 p-5">
      <p className="text-sm font-bold text-red-500" role="alert">{message}</p>
      <button
        type="button"
        onClick={() => runAsyncAction(onRetry)}
        className="mt-4 rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
      >
        Retry
      </button>
    </div>
  );
}

function AdminAccountRow({ account, updatingAccountIban, toggleAdminAccountStatus }: AdminAccountRowProps) {
  const action = getAdminAccountAction(account.status);
  const isUpdating = updatingAccountIban === account.iban;
  const actionLabel = getAdminAccountActionLabel(isUpdating, action);

  return (
    <div className="grid gap-3 rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] p-4 lg:grid-cols-[1fr_auto_auto] lg:items-center">
      <div>
        <p className="font-bold text-[rgb(var(--text-strong))]">{account.name}</p>
        <p className="mt-1 font-mono text-xs font-bold text-[rgb(var(--text-muted))]">{account.iban} · {account.currency}</p>
      </div>
      <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${getAdminAccountStatusClass(account.status)}`}>
        {account.status}
      </span>
      <button
        type="button"
        onClick={() => runAsyncAction(() => toggleAdminAccountStatus(account))}
        disabled={Boolean(updatingAccountIban)}
        className="rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))] disabled:cursor-not-allowed disabled:opacity-60"
      >
        {actionLabel}
      </button>
    </div>
  );
}

function AdminUserCard({ user, updatingAccountIban, toggleAdminAccountStatus }: AdminUserCardProps) {
  return (
    <div className="grid gap-4 py-5">
      <div className="grid gap-3 lg:grid-cols-[1fr_auto_auto] lg:items-center">
        <div>
          <p className="font-bold text-[rgb(var(--text-strong))]">{user.firstName} {user.lastName}</p>
          <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">
            {user.email} · {user.accountCount} account{user.accountCount === 1 ? '' : 's'}
          </p>
        </div>
        <span className="text-sm font-extrabold text-[rgb(var(--gold))]">{user.role}</span>
        <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${getAdminUserStatusClass(user.active)}`}>
          {user.active ? 'Active' : 'Inactive'}
        </span>
      </div>
      <div className="grid gap-3">
        {user.accounts.length > 0 ? (
          user.accounts.map((account) => (
            <AdminAccountRow
              key={account.iban}
              account={account}
              updatingAccountIban={updatingAccountIban}
              toggleAdminAccountStatus={toggleAdminAccountStatus}
            />
          ))
        ) : (
          <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-muted))]">
            No accounts for this user.
          </p>
        )}
      </div>
    </div>
  );
}

function AdminUsersPanel({
  searchQuery,
  setSearchQuery,
  accountActionError,
  isLoadingUsers,
  usersError,
  filteredUsers,
  updatingAccountIban,
  loadUsers,
  toggleAdminAccountStatus,
}: AdminUsersPanelProps) {
  let content = (
    <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-4 text-sm font-bold text-[rgb(var(--text-muted))]">
      No users found.
    </p>
  );

  if (isLoadingUsers) {
    content = (
      <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-4 text-sm font-bold text-[rgb(var(--text-muted))]">
        Loading users...
      </p>
    );
  } else if (usersError) {
    content = <AdminRetryPanel message={usersError} onRetry={loadUsers} />;
  } else if (filteredUsers.length > 0) {
    content = (
      <div className="divide-y divide-[rgb(var(--line))]">
        {filteredUsers.map((user) => (
          <AdminUserCard
            key={user.email}
            user={user}
            updatingAccountIban={updatingAccountIban}
            toggleAdminAccountStatus={toggleAdminAccountStatus}
          />
        ))}
      </div>
    );
  }

  return (
    <div>
      <label className="mb-5 block">
        <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Search Users</span>
        <input
          value={searchQuery}
          onChange={(event) => setSearchQuery(event.target.value)}
          className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
          placeholder="Search by name, email, account, IBAN, or status"
        />
      </label>
      {accountActionError && (
        <p className="mb-4 rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm font-bold text-red-500" role="alert">
          {accountActionError}
        </p>
      )}
      {content}
    </div>
  );
}

function AdminTransactionRow({ transaction }: Readonly<{ transaction: AdminTransactionResponse }>) {
  const amount = formatCurrencyAmount(Number(transaction.amount), transaction.sourceCurrency);
  const creditedAmount = formatCurrencyAmount(Number(transaction.creditedAmount), transaction.destinationCurrency);
  const statusClass = transaction.status === 'FAILED' ? 'bg-red-500/15 text-red-500' : 'bg-emerald-500/15 text-emerald-500';

  return (
    <div className="grid gap-3 py-4 xl:grid-cols-[0.65fr_1fr_auto_auto] xl:items-center">
      <span className="font-mono text-sm font-bold text-[rgb(var(--gold))]">TRF-{transaction.transactionId}</span>
      <div>
        <p className="break-all font-mono text-xs font-bold text-[rgb(var(--text-strong))]">{transaction.sourceIban}</p>
        <p className="mt-1 break-all font-mono text-xs font-bold text-[rgb(var(--text-muted))]">{transaction.destinationIban}</p>
        <p className="mt-2 text-sm font-semibold text-[rgb(var(--text-muted))]">
          {transaction.reason} · {formatProfileDate(transaction.timeStamp, 'Date unavailable')}
        </p>
      </div>
      <div className="text-left xl:text-right">
        <span className="block font-display text-xl font-bold text-[rgb(var(--text-strong))]">{amount}</span>
        <span className="mt-1 block text-xs font-bold text-[rgb(var(--text-muted))]">Credited {creditedAmount}</span>
      </div>
      <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${statusClass}`}>
        {transaction.status}
      </span>
    </div>
  );
}

function AdminTransactionsPanel({
  isLoadingTransactions,
  transactionsError,
  transactions,
  loadTransactions,
}: AdminTransactionsPanelProps) {
  if (isLoadingTransactions) {
    return (
      <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-4 text-sm font-bold text-[rgb(var(--text-muted))]">
        Loading transfer logs...
      </p>
    );
  }

  if (transactionsError) {
    return <AdminRetryPanel message={transactionsError} onRetry={loadTransactions} />;
  }

  if (transactions.length === 0) {
    return (
      <p className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-4 text-sm font-bold text-[rgb(var(--text-muted))]">
        No transfer logs found.
      </p>
    );
  }

  return (
    <div className="divide-y divide-[rgb(var(--line))]">
      {transactions.map((transaction) => (
        <AdminTransactionRow key={transaction.transactionId} transaction={transaction} />
      ))}
    </div>
  );
}

function AdminPage({ authSession, showHome }: AdminPageProps) {
  const [activeMenu, setActiveMenu] = React.useState<AdminMenuId>('pending');
  const [searchQuery, setSearchQuery] = React.useState('');
  const [users, setUsers] = React.useState<AdminUserResponse[]>([]);
  const [transactions, setTransactions] = React.useState<AdminTransactionResponse[]>([]);
  const [isLoadingUsers, setIsLoadingUsers] = React.useState(false);
  const [isLoadingTransactions, setIsLoadingTransactions] = React.useState(false);
  const [usersError, setUsersError] = React.useState('');
  const [transactionsError, setTransactionsError] = React.useState('');
  const [accountActionError, setAccountActionError] = React.useState('');
  const [updatingAccountIban, setUpdatingAccountIban] = React.useState<string | null>(null);
  const activeMenuDetails = adminMenus.find((menu) => menu.id === activeMenu) ?? adminMenus[0];
  const normalizedSearchQuery = searchQuery.trim().toLowerCase();
  const filteredUsers = normalizedSearchQuery
    ? users.filter((user) => {
      const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
      const accountMatch = user.accounts.some((account) => (
        account.iban.toLowerCase().includes(normalizedSearchQuery)
        || account.name.toLowerCase().includes(normalizedSearchQuery)
        || account.status.toLowerCase().includes(normalizedSearchQuery)
      ));
      return fullName.includes(normalizedSearchQuery)
        || user.email.toLowerCase().includes(normalizedSearchQuery)
        || user.role.toLowerCase().includes(normalizedSearchQuery)
        || accountMatch;
    })
    : users;

  const loadUsers = React.useCallback(async () => {
    setIsLoadingUsers(true);
    setUsersError('');
    setAccountActionError('');
    try {
      setUsers(await fetchAdminUsers(authSession));
    } catch (error) {
      setUsersError(error instanceof Error ? error.message : 'Unable to load users.');
    } finally {
      setIsLoadingUsers(false);
    }
  }, [authSession]);

  const loadTransactions = React.useCallback(async () => {
    setIsLoadingTransactions(true);
    setTransactionsError('');
    try {
      setTransactions(await fetchAdminTransactions(authSession));
    } catch (error) {
      setTransactionsError(error instanceof Error ? error.message : 'Unable to load transfer logs.');
    } finally {
      setIsLoadingTransactions(false);
    }
  }, [authSession]);

  React.useEffect(() => {
    if (activeMenu === 'users') {
      runAsyncAction(loadUsers);
    }
    if (activeMenu === 'logs') {
      runAsyncAction(loadTransactions);
    }
  }, [activeMenu, loadTransactions, loadUsers]);

  async function toggleAdminAccountStatus(account: AccountResponse) {
    const action = account.status === 'BLOCKED' ? 'unblock' : 'block';
    setUpdatingAccountIban(account.iban);
    setAccountActionError('');
    try {
      const updatedAccount = await updateAdminAccountStatus(authSession, account.iban, action);
      setUsers((currentUsers) => updateUsersWithAccount(currentUsers, updatedAccount));
    } catch (error) {
      setAccountActionError(error instanceof Error ? error.message : `Unable to ${action} account.`);
    } finally {
      setUpdatingAccountIban(null);
    }
  }

  return (
    <section className="min-h-screen bg-[rgb(var(--page-bg))] px-6 pb-20 pt-28 sm:px-10 lg:pt-32">
      <div className="mx-auto max-w-[1180px]">
        <div className="flex flex-col justify-between gap-6 lg:flex-row lg:items-end">
          <div>
            <button
              type="button"
              onClick={showHome}
              className="inline-flex items-center gap-2 text-sm font-bold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))]"
            >
              <ArrowLeft size={16} strokeWidth={1.8} />
              Back to website
            </button>
            <p className="mt-8 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">Admin Console</p>
            <h1 className="mt-3 font-display text-[clamp(2.8rem,4vw,4.6rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
              Operations Dashboard
            </h1>
          </div>
          <div className="flex max-w-[420px] items-center gap-3 rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] px-4 py-3">
            <Search size={17} className="text-[rgb(var(--text-muted))]" />
            <input
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              className="w-full bg-transparent text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]"
              placeholder="Search clients, transfers, reviews"
            />
            <Settings size={17} className="text-[rgb(var(--gold))]" />
          </div>
        </div>

        <div className="mt-10 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {adminMenus.map(({ id, label, description, icon: Icon }) => (
            <button
              key={id}
              type="button"
              onClick={() => setActiveMenu(id)}
              className={`rounded-lg border p-5 text-left shadow-vault transition hover:-translate-y-0.5 ${
                activeMenu === id
                  ? 'border-[rgb(var(--gold))] bg-[rgb(var(--icon-bg))]'
                  : 'border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] hover:border-[rgb(var(--gold))]/70'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-[0.62rem] font-extrabold uppercase tracking-[0.24em] text-[rgb(var(--gold))]">
                    Menu
                  </p>
                  <p className="mt-3 font-display text-2xl font-bold leading-none text-[rgb(var(--text-strong))]">{label}</p>
                </div>
                <div className="grid h-10 w-10 shrink-0 place-items-center rounded-full border border-[rgb(var(--gold))]/30 bg-[rgb(var(--page-bg))] text-[rgb(var(--gold))]">
                  <Icon size={18} strokeWidth={1.8} />
                </div>
              </div>
              <p className="mt-4 text-sm font-semibold leading-6 text-[rgb(var(--text-muted))]">{description}</p>
            </button>
          ))}
        </div>

        <div className="mt-8 grid gap-8 lg:grid-cols-[260px_1fr]">
          <aside className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-3">
            <div className="px-3 py-3">
              <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.28em] text-[rgb(var(--gold))]">Admin Menus</p>
            </div>
            <div className="grid gap-2">
              {adminMenus.map(({ id, label, icon: Icon }) => (
                <button
                  key={id}
                  type="button"
                  onClick={() => setActiveMenu(id)}
                  className={`flex items-center gap-3 rounded-md px-3 py-3 text-left text-sm font-extrabold transition ${
                    activeMenu === id
                      ? 'bg-[rgb(var(--gold))] text-[rgb(var(--gold-ink))]'
                      : 'text-[rgb(var(--text-muted))] hover:bg-[rgb(var(--page-bg))] hover:text-[rgb(var(--text-strong))]'
                  }`}
                >
                  <Icon size={17} strokeWidth={1.8} />
                  <span>{label}</span>
                </button>
              ))}
            </div>
          </aside>

          <div className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6">
            <div className="mb-6 flex flex-col justify-between gap-4 border-b border-[rgb(var(--line))] pb-5 sm:flex-row sm:items-center">
              <div>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">Selected Menu</p>
                <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">{activeMenuDetails.label}</h2>
                <p className="mt-2 text-sm font-semibold text-[rgb(var(--text-muted))]">{activeMenuDetails.description}</p>
              </div>
              <span className="rounded-full bg-[rgb(var(--icon-bg))] px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] text-[rgb(var(--gold))]">
                {activeMenu === 'users' || activeMenu === 'logs' ? 'Live API' : 'Frontend Demo'}
              </span>
            </div>

            {activeMenu === 'pending' && (
              <div className="divide-y divide-[rgb(var(--line))]">
                {clientQueue.map((item) => (
                  <div key={`${item.name}-${item.request}`} className="grid gap-4 py-4 xl:grid-cols-[1fr_auto_auto] xl:items-center">
                    <div>
                      <p className="font-bold text-[rgb(var(--text-strong))]">{item.name}</p>
                      <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{item.request} · {item.time}</p>
                    </div>
                    <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${getClientQueuePriorityClass(item.priority)}`}>
                      {item.priority}
                    </span>
                    <div className="flex gap-2">
                      <button type="button" className="rounded-md bg-[rgb(var(--gold))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--gold-ink))]">
                        Approve
                      </button>
                      <button type="button" className="rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--text-strong))]">
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {activeMenu === 'users' && (
              <AdminUsersPanel
                searchQuery={searchQuery}
                setSearchQuery={setSearchQuery}
                accountActionError={accountActionError}
                isLoadingUsers={isLoadingUsers}
                usersError={usersError}
                filteredUsers={filteredUsers}
                updatingAccountIban={updatingAccountIban}
                loadUsers={loadUsers}
                toggleAdminAccountStatus={toggleAdminAccountStatus}
              />
            )}

            {activeMenu === 'logs' && (
              <AdminTransactionsPanel
                isLoadingTransactions={isLoadingTransactions}
                transactionsError={transactionsError}
                transactions={transactions}
                loadTransactions={loadTransactions}
              />
            )}

            {activeMenu === 'access' && (
              <div className="grid gap-6 xl:grid-cols-[1fr_0.85fr]">
                <div>
                  <p className="mb-4 text-sm font-extrabold text-[rgb(var(--text-strong))]">Pending Access Requests</p>
                  <div className="divide-y divide-[rgb(var(--line))]">
                    {accessRequests.map((request) => (
                      <div key={request.email} className="grid gap-3 py-4">
                        <div>
                          <p className="font-bold text-[rgb(var(--text-strong))]">{request.name}</p>
                          <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{request.email} · {request.request} · {request.submitted}</p>
                        </div>
                        <div className="flex flex-wrap gap-2">
                          <button type="button" className="rounded-md bg-[rgb(var(--gold))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--gold-ink))]">
                            Approve Admin
                          </button>
                          <button type="button" className="rounded-md border border-[rgb(var(--button-line))] px-4 py-2 text-xs font-extrabold text-[rgb(var(--text-strong))]">
                            Create User
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                <form className="rounded-lg border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] p-5" onSubmit={(event) => event.preventDefault()}>
                  <p className="font-display text-2xl font-semibold text-[rgb(var(--text-strong))]">Create Website User</p>
                  <div className="mt-5 grid gap-4">
                    <label>
                      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Full Name</span>
                      <input className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--card-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]" placeholder="Client name" />
                    </label>
                    <label>
                      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Email</span>
                      <input className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--card-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]" placeholder="client@example.com" />
                    </label>
                    <label>
                      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Role</span>
                      <select className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--card-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]">
                        <option>Client</option>
                        <option>Advisor</option>
                        <option>Admin</option>
                      </select>
                    </label>
                    <button type="submit" className="rounded-md bg-[rgb(var(--gold))] px-5 py-3 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold">
                      Create User
                    </button>
                  </div>
                </form>
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}

function AdminAccessGate({
  authSession,
  openAuth,
  showHome,
}: {
  authSession: AuthSession | null;
  openAuth: (mode: AuthMode) => void;
  showHome: () => void;
}) {
  const isLoggedIn = Boolean(authSession);

  return (
    <section className="pattern-bg flex min-h-screen items-center px-6 py-28 sm:px-10">
      <div className="mx-auto w-full max-w-[620px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-8 text-center shadow-vault">
        <div className="mx-auto grid h-14 w-14 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <ShieldCheck size={24} strokeWidth={1.8} />
        </div>
        <p className="mt-7 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">
          Admin Access Required
        </p>
        <h1 className="mt-4 font-display text-[clamp(2.7rem,4vw,4rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
          {isLoggedIn ? 'Admin Role Required' : 'Login Required'}
        </h1>
        <p className="mx-auto mt-6 max-w-[500px] text-base leading-7 text-[rgb(var(--text-muted))]">
          {isLoggedIn
            ? 'Your account is signed in, but it does not have admin permissions for this panel.'
            : 'Sign in with an admin account to access operational tools, user search, transfer logs, and admin approvals.'}
        </p>
        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          {!isLoggedIn && (
            <button
              type="button"
              onClick={() => openAuth('login')}
              className="rounded-md bg-[rgb(var(--gold))] px-7 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
            >
              Admin Login
            </button>
          )}
          <button
            type="button"
            onClick={showHome}
            className="rounded-md border border-[rgb(var(--button-line))] px-7 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
          >
            Back to Website
          </button>
        </div>
      </div>
    </section>
  );
}

function AdminRoute({
  authSession,
  openAuth,
  showHome,
}: {
  authSession: AuthSession | null;
  openAuth: (mode: AuthMode) => void;
  showHome: () => void;
}) {
  const [isChecking, setIsChecking] = React.useState(Boolean(authSession));
  const [isAllowed, setIsAllowed] = React.useState(false);

  React.useEffect(() => {
    let isMounted = true;

    if (!authSession) {
      setIsChecking(false);
      setIsAllowed(false);
      return () => {
        isMounted = false;
      };
    }

    setIsChecking(true);
    verifyAdminSession(authSession)
      .then(() => {
        if (isMounted) setIsAllowed(true);
      })
      .catch(() => {
        if (isMounted) setIsAllowed(false);
      })
      .finally(() => {
        if (isMounted) setIsChecking(false);
      });

    return () => {
      isMounted = false;
    };
  }, [authSession]);

  if (isChecking) {
    return (
      <section className="pattern-bg flex min-h-screen items-center justify-center px-6 py-28 text-center sm:px-10">
        <div>
          <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">Admin Console</p>
          <h1 className="mt-4 font-display text-[clamp(2.7rem,4vw,4rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
            Verifying Access
          </h1>
        </div>
      </section>
    );
  }

  if (!authSession || !isAllowed) {
    return <AdminAccessGate authSession={authSession} openAuth={openAuth} showHome={showHome} />;
  }

  return <AdminPage authSession={authSession} showHome={showHome} />;
}

function Cta({ showTransactions, showPortfolio }: { showTransactions: () => void; showPortfolio: () => void }) {
  return (
    <section id="contact" className="pattern-bg border-t border-[rgb(var(--line))] px-6 py-32 text-center sm:px-10 lg:px-12">
      <div className="mx-auto max-w-[720px]">
        <Eyebrow>Begin Your Journey</Eyebrow>
        <h2 className="mt-8 font-display text-[clamp(3rem,3.4vw,4.2rem)] font-semibold leading-[1.05] text-[rgb(var(--text-strong))]">
          Become Part of
          <br />
          Our Legacy
        </h2>
        <p className="mx-auto mt-9 max-w-[650px] text-lg leading-8 text-[rgb(var(--text-muted))]">
          Join twelve thousand distinguished families who have chosen SAFE Bank as the steward of their wealth. Experience banking as it was meant to be, personal, refined, and enduring.
        </p>
        <div className="mt-12 flex flex-col justify-center gap-4 sm:flex-row">
          <button
            type="button"
            onClick={showTransactions}
            className="rounded-md bg-[rgb(var(--gold))] px-9 py-4 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
          >
            Transfer Funds
          </button>
          <button
            type="button"
            onClick={showPortfolio}
            className="rounded-md border border-[rgb(var(--button-line))] px-9 py-4 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
          >
            Portfolio
          </button>
        </div>
        <p className="mt-8 text-sm font-semibold text-[rgb(var(--text-muted))]">
          By invitation and introduction · FDIC insured · Serving families since 1847
        </p>
      </div>
    </section>
  );
}

type AuthPopupProps = {
  mode: AuthMode;
  isOpen: boolean;
  onClose: () => void;
  onModeChange: (mode: AuthMode) => void;
  onAuthenticated: (result: AuthenticationResult) => void;
};

type AuthFormViewModel = {
  isLogin: boolean;
  latestBirthDate: string;
  AuthEntryVisibilityIcon: LucideIcon;
  authEntryInputType: 'text' | 'password';
  authEntryAutoComplete: string;
  authEntryMinLength?: number;
  authEntryPattern?: string;
  authEntryDescriptionId?: string;
  authEntryPlaceholder: string;
  confirmAuthEntryErrorId?: string;
  submitButtonText: string;
};

function getAuthFormViewModel(
  mode: AuthMode,
  showPassword: boolean,
  fieldErrors: AuthFieldErrors,
  isSubmitting: boolean,
): AuthFormViewModel {
  const isLogin = mode === 'login';
  const authSubmitText = isLogin ? 'Login Securely' : 'Register';
  const authEntryErrorId = fieldErrors.password ? AUTH_FIELD_IDS.entryError : undefined;
  const authEntryRequirementsId = isLogin ? undefined : AUTH_FIELD_IDS.entryRequirements;

  return {
    isLogin,
    latestBirthDate: getLatestValidBirthDate(),
    AuthEntryVisibilityIcon: showPassword ? EyeOff : Eye,
    authEntryInputType: showPassword ? 'text' : 'password',
    authEntryAutoComplete: isLogin ? AUTH_INPUT_AUTOCOMPLETE.loginEntry : AUTH_INPUT_AUTOCOMPLETE.newEntry,
    authEntryMinLength: isLogin ? undefined : 10,
    authEntryPattern: isLogin ? undefined : REGISTRATION_AUTH_PATTERN,
    authEntryDescriptionId: authEntryErrorId ?? authEntryRequirementsId,
    authEntryPlaceholder: isLogin ? AUTH_FIELD_PLACEHOLDERS.loginEntry : AUTH_FIELD_PLACEHOLDERS.newEntry,
    confirmAuthEntryErrorId: fieldErrors.confirmPassword ? AUTH_FIELD_IDS.confirmEntryError : undefined,
    submitButtonText: isSubmitting ? 'Please wait...' : authSubmitText,
  };
}

function buildAuthPayload(mode: AuthMode, formData: FormData): Record<string, string> {
  const email = getFormString(formData, 'email', true);
  const password = getFormString(formData, 'password');

  if (mode === 'login') {
    return { email, password };
  }

  return {
    email,
    password,
    firstName: getFormString(formData, 'firstName', true),
    lastName: getFormString(formData, 'lastName', true),
    dateOfBirth: getFormString(formData, 'dateOfBirth'),
    ePin: getFormString(formData, 'ePin', true),
  };
}

function useAuthPopupLifecycle(mode: AuthMode, isOpen: boolean, onClose: () => void, resetState: () => void) {
  React.useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose();
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleKeyDown);

    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  React.useEffect(() => {
    resetState();
  }, [mode, isOpen, resetState]);
}

function AuthPopupIntro({ isLogin }: { isLogin: boolean }) {
  const IntroIcon = isLogin ? LockKeyhole : UserPlus;
  const title = isLogin ? 'Welcome Back' : 'Request Access';
  const description = isLogin
    ? 'Sign in to review your portfolio, advisory notes, and private banking activity.'
    : 'Create an access request and a private banking advisor will review your introduction.';

  return (
    <div className="pr-10">
      <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
        <IntroIcon size={20} strokeWidth={1.8} />
      </div>
      <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">
        Secure Client Access
      </p>
      <h2 id="auth-title" className="mt-3 font-display text-4xl font-semibold leading-tight text-[rgb(var(--text-strong))]">
        {title}
      </h2>
      <p className="mt-3 text-sm leading-6 text-[rgb(var(--text-muted))]">
        {description}
      </p>
    </div>
  );
}

function AuthModeTabs({ mode, onModeChange }: { mode: AuthMode; onModeChange: (mode: AuthMode) => void }) {
  return (
    <div className="mt-7 grid grid-cols-2 rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] p-1">
      {(['login', 'register'] as AuthMode[]).map((item) => (
        <button
          key={item}
          type="button"
          onClick={() => onModeChange(item)}
          className={`rounded px-4 py-2.5 text-sm font-extrabold capitalize transition ${
            mode === item
              ? 'bg-[rgb(var(--gold))] text-[rgb(var(--gold-ink))]'
              : 'text-[rgb(var(--text-muted))] hover:text-[rgb(var(--text-strong))]'
          }`}
        >
          {item}
        </button>
      ))}
    </div>
  );
}

function RegistrationFields({
  isLogin,
  fieldErrors,
  latestBirthDate,
}: {
  isLogin: boolean;
  fieldErrors: AuthFieldErrors;
  latestBirthDate: string;
}) {
  if (isLogin) return null;

  return (
    <>
      <div className="grid gap-4 sm:grid-cols-2">
        <label className="block">
          <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
            First Name
          </span>
          <input
            name="firstName"
            type="text"
            autoComplete="given-name"
            pattern="^[^0-9]+$"
            aria-invalid={Boolean(fieldErrors.firstName)}
            aria-describedby={fieldErrors.firstName ? 'first-name-error' : undefined}
            className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
              fieldErrors.firstName ? 'border-red-500' : 'border-[rgb(var(--line))]'
            }`}
            placeholder="Alex"
            required
          />
          {fieldErrors.firstName && (
            <p id="first-name-error" className="mt-2 text-xs font-bold text-red-500">
              {fieldErrors.firstName}
            </p>
          )}
        </label>
        <label className="block">
          <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
            Last Name
          </span>
          <input
            name="lastName"
            type="text"
            autoComplete="family-name"
            pattern="^[^0-9]+$"
            aria-invalid={Boolean(fieldErrors.lastName)}
            aria-describedby={fieldErrors.lastName ? 'last-name-error' : undefined}
            className={`w-full rounded-md border bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
              fieldErrors.lastName ? 'border-red-500' : 'border-[rgb(var(--line))]'
            }`}
            placeholder="Morgan"
            required
          />
          {fieldErrors.lastName && (
            <p id="last-name-error" className="mt-2 text-xs font-bold text-red-500">
              {fieldErrors.lastName}
            </p>
          )}
        </label>
      </div>

      <label className="block">
        <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
          Date of Birth
        </span>
        <div className="relative">
          <Calendar className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[rgb(var(--text-muted))]" size={16} />
          <input
            name="dateOfBirth"
            type="date"
            autoComplete="bday"
            max={latestBirthDate}
            aria-invalid={Boolean(fieldErrors.dateOfBirth)}
            aria-describedby={fieldErrors.dateOfBirth ? 'date-of-birth-error' : undefined}
            className={`w-full rounded-md border bg-[rgb(var(--page-bg))] py-3 pl-11 pr-4 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
              fieldErrors.dateOfBirth ? 'border-red-500' : 'border-[rgb(var(--line))]'
            }`}
            required
          />
        </div>
        {fieldErrors.dateOfBirth && (
          <p id="date-of-birth-error" className="mt-2 text-xs font-bold text-red-500">
            {fieldErrors.dateOfBirth}
          </p>
        )}
      </label>

      <label className="block">
        <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
          E-PIN <span className="normal-case tracking-normal text-[rgb(var(--text-muted))]">(optional)</span>
        </span>
        <div className="relative">
          <KeyRound className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[rgb(var(--text-muted))]" size={16} />
          <input
            name="ePin"
            type="password"
            inputMode="numeric"
            autoComplete="off"
            minLength={EPIN_LENGTH}
            maxLength={EPIN_LENGTH}
            pattern={EPIN_INPUT_PATTERN}
            onInput={(event) => {
              event.currentTarget.value = sanitizeEPin(event.currentTarget.value);
            }}
            aria-invalid={Boolean(fieldErrors.ePin)}
            aria-describedby={fieldErrors.ePin ? 'registration-epin-error' : 'registration-epin-help'}
            className={`w-full rounded-md border bg-[rgb(var(--page-bg))] py-3 pl-11 pr-4 text-sm font-semibold tracking-[0.18em] text-[rgb(var(--text-strong))] outline-none transition placeholder:tracking-normal placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
              fieldErrors.ePin ? 'border-red-500' : 'border-[rgb(var(--line))]'
            }`}
            placeholder={`${EPIN_LENGTH} digits`}
          />
        </div>
        {fieldErrors.ePin ? (
          <p id="registration-epin-error" className="mt-2 text-xs font-bold text-red-500">
            {fieldErrors.ePin}
          </p>
        ) : (
          <p id="registration-epin-help" className="mt-2 text-xs font-semibold text-[rgb(var(--text-muted))]">
            Leave blank and a secure 6-digit E-PIN will be generated.
          </p>
        )}
      </label>
    </>
  );
}

function EmailField({ fieldErrors }: { fieldErrors: AuthFieldErrors }) {
  return (
    <label className="block">
      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
        Email Address
      </span>
      <div className="relative">
        <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[rgb(var(--text-muted))]" size={16} />
        <input
          name="email"
          type="email"
          autoComplete="email"
          aria-invalid={Boolean(fieldErrors.email)}
          aria-describedby={fieldErrors.email ? 'email-error' : undefined}
          className={`w-full rounded-md border bg-[rgb(var(--page-bg))] py-3 pl-11 pr-4 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
            fieldErrors.email ? 'border-red-500' : 'border-[rgb(var(--line))]'
          }`}
          placeholder="client@example.com"
          required
        />
      </div>
      {fieldErrors.email && (
        <p id="email-error" className="mt-2 text-xs font-bold text-red-500">
          {fieldErrors.email}
        </p>
      )}
    </label>
  );
}

function AuthEntryField({
  isLogin,
  fieldErrors,
  view,
  showPassword,
  togglePasswordVisibility,
}: {
  isLogin: boolean;
  fieldErrors: AuthFieldErrors;
  view: AuthFormViewModel;
  showPassword: boolean;
  togglePasswordVisibility: () => void;
}) {
  const VisibilityIcon = view.AuthEntryVisibilityIcon;

  return (
    <label className="block">
      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
        Password
      </span>
      <div className="relative">
        <input
          name="password"
          type={view.authEntryInputType}
          autoComplete={view.authEntryAutoComplete}
          minLength={view.authEntryMinLength}
          pattern={view.authEntryPattern}
          aria-invalid={Boolean(fieldErrors.password)}
          aria-describedby={view.authEntryDescriptionId}
          className={`w-full rounded-md border bg-[rgb(var(--page-bg))] py-3 pl-4 pr-12 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
            fieldErrors.password ? 'border-red-500' : 'border-[rgb(var(--line))]'
          }`}
          placeholder={view.authEntryPlaceholder}
          required
        />
        <button
          type="button"
          onClick={togglePasswordVisibility}
          className="absolute right-3 top-1/2 grid h-8 w-8 -translate-y-1/2 place-items-center rounded-full text-[rgb(var(--text-muted))] transition hover:bg-[rgb(var(--line))] hover:text-[rgb(var(--text-strong))]"
          aria-label={showPassword ? 'Hide password' : 'Show password'}
          aria-pressed={showPassword}
        >
          <VisibilityIcon size={17} strokeWidth={1.8} />
        </button>
      </div>
      {!isLogin && !fieldErrors.password && (
        <p id={AUTH_FIELD_IDS.entryRequirements} className="mt-2 text-xs font-semibold leading-5 text-[rgb(var(--text-muted))]">
          Minimum 10 characters with 1 uppercase letter, 1 number, and 1 special character.
        </p>
      )}
      {fieldErrors.password && (
        <p id={AUTH_FIELD_IDS.entryError} className="mt-2 text-xs font-bold leading-5 text-red-500">
          {fieldErrors.password}
        </p>
      )}
    </label>
  );
}

function ConfirmAuthEntryField({
  isLogin,
  fieldErrors,
  view,
  showPassword,
  togglePasswordVisibility,
}: {
  isLogin: boolean;
  fieldErrors: AuthFieldErrors;
  view: AuthFormViewModel;
  showPassword: boolean;
  togglePasswordVisibility: () => void;
}) {
  const VisibilityIcon = view.AuthEntryVisibilityIcon;

  if (isLogin) return null;

  return (
    <label className="block">
      <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
        Confirm Password
      </span>
      <div className="relative">
        <input
          name="confirmPassword"
          type={view.authEntryInputType}
          autoComplete={AUTH_INPUT_AUTOCOMPLETE.newEntry}
          minLength={10}
          aria-invalid={Boolean(fieldErrors.confirmPassword)}
          aria-describedby={view.confirmAuthEntryErrorId}
          className={`w-full rounded-md border bg-[rgb(var(--page-bg))] py-3 pl-4 pr-12 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))] ${
            fieldErrors.confirmPassword ? 'border-red-500' : 'border-[rgb(var(--line))]'
          }`}
          placeholder={AUTH_FIELD_PLACEHOLDERS.confirmEntry}
          required
        />
        <button
          type="button"
          onClick={togglePasswordVisibility}
          className="absolute right-3 top-1/2 grid h-8 w-8 -translate-y-1/2 place-items-center rounded-full text-[rgb(var(--text-muted))] transition hover:bg-[rgb(var(--line))] hover:text-[rgb(var(--text-strong))]"
          aria-label={showPassword ? 'Hide password' : 'Show password'}
          aria-pressed={showPassword}
        >
          <VisibilityIcon size={17} strokeWidth={1.8} />
        </button>
      </div>
      {fieldErrors.confirmPassword && (
        <p id={AUTH_FIELD_IDS.confirmEntryError} className="mt-2 text-xs font-bold leading-5 text-red-500">
          {fieldErrors.confirmPassword}
        </p>
      )}
    </label>
  );
}

function AuthFeedbackMessages({ error, success }: { error: string; success: string }) {
  return (
    <>
      {error && (
        <p className="rounded-md border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm font-bold text-red-500" role="alert">
          {error}
        </p>
      )}

      {success && (
        <output className="block rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm font-bold text-emerald-500">
          {success}
        </output>
      )}
    </>
  );
}

function AuthModePrompt({ isLogin, onModeChange }: { isLogin: boolean; onModeChange: (mode: AuthMode) => void }) {
  return (
    <p className="mt-5 text-center text-xs font-semibold leading-5 text-[rgb(var(--text-muted))]">
      {isLogin ? 'Need an invitation?' : 'Already approved?'}{' '}
      <button
        type="button"
        onClick={() => onModeChange(isLogin ? 'register' : 'login')}
        className="font-extrabold text-[rgb(var(--gold))] hover:underline"
      >
        {isLogin ? 'Request access' : 'Login instead'}
      </button>
    </p>
  );
}

function AuthPopup({
  mode,
  isOpen,
  onClose,
  onModeChange,
  onAuthenticated,
}: AuthPopupProps) {
  const [error, setError] = React.useState('');
  const [success, setSuccess] = React.useState('');
  const [fieldErrors, setFieldErrors] = React.useState<AuthFieldErrors>({});
  const [showPassword, setShowPassword] = React.useState(false);
  const [isSubmitting, setIsSubmitting] = React.useState(false);

  const resetState = React.useCallback(() => {
    setError('');
    setSuccess('');
    setFieldErrors({});
    setShowPassword(false);
    setIsSubmitting(false);
  }, []);

  useAuthPopupLifecycle(mode, isOpen, onClose, resetState);

  if (!isOpen) return null;

  const view = getAuthFormViewModel(mode, showPassword, fieldErrors, isSubmitting);
  const togglePasswordVisibility = () => setShowPassword((current) => !current);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    setSuccess('');
    setFieldErrors({});

    const formData = new FormData(event.currentTarget);
    const validationErrors = view.isLogin ? {} : getRegistrationValidationErrors(formData);

    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      setError('Please fix the highlighted registration fields.');
      return;
    }

    setIsSubmitting(true);

    try {
      const session = await authenticate(mode, buildAuthPayload(mode, formData));
      onAuthenticated(session);
      setSuccess(view.isLogin ? 'Login successful.' : 'Registration successful.');
      onClose();
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : 'Authentication failed. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center px-5 py-8">
      <button
        type="button"
        aria-label="Close authentication popup"
        className="absolute inset-0 bg-black/65 backdrop-blur-sm"
        onClick={onClose}
      />
      <dialog
        open
        aria-labelledby="auth-title"
        className="relative max-h-[calc(100vh-4rem)] w-full max-w-[460px] overflow-y-auto rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--line))] text-[rgb(var(--text-muted))] transition hover:border-[rgb(var(--gold))] hover:text-[rgb(var(--text-strong))]"
          aria-label="Close popup"
        >
          <X size={17} strokeWidth={1.8} />
        </button>

        <AuthPopupIntro isLogin={view.isLogin} />

        <AuthModeTabs mode={mode} onModeChange={onModeChange} />

        <form
          className="mt-7 space-y-4"
          onSubmit={handleSubmit}
          noValidate={!view.isLogin}
        >
          <RegistrationFields
            isLogin={view.isLogin}
            fieldErrors={fieldErrors}
            latestBirthDate={view.latestBirthDate}
          />

          <EmailField fieldErrors={fieldErrors} />

          <AuthEntryField
            isLogin={view.isLogin}
            fieldErrors={fieldErrors}
            view={view}
            showPassword={showPassword}
            togglePasswordVisibility={togglePasswordVisibility}
          />

          <ConfirmAuthEntryField
            isLogin={view.isLogin}
            fieldErrors={fieldErrors}
            view={view}
            showPassword={showPassword}
            togglePasswordVisibility={togglePasswordVisibility}
          />

          <AuthFeedbackMessages error={error} success={success} />

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70 disabled:hover:translate-y-0"
          >
            {view.submitButtonText}
          </button>
        </form>

        <AuthModePrompt isLogin={view.isLogin} onModeChange={onModeChange} />
      </dialog>
    </div>
  );
}

function Footer() {
  const groups = [
    ['Banking', 'Transactions', 'Portfolio', 'Private Accounts', 'Support'],
    ['About', 'Portfolio', 'Leadership', 'Locations', 'Insights'],
    ['Legal', 'Privacy Policy', 'Terms of Service', 'Disclosures', 'Compliance'],
  ];

  return (
    <footer className="border-t border-[rgb(var(--line))] bg-[rgb(var(--footer-bg))] px-6 py-16 sm:px-10 lg:px-12">
      <div className="mx-auto max-w-[980px]">
        <div className="grid gap-12 md:grid-cols-[1.6fr_1fr_1fr_1fr]">
          <div>
            <Logo />
            <p className="mt-7 max-w-[280px] text-base leading-7 text-[rgb(var(--text-muted))]">
              Preserving wealth and legacy for distinguished families since 1847.
            </p>
          </div>
          {groups.map(([title, ...links]) => (
            <div key={title}>
              <h3 className="text-[0.72rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--text-strong))]">{title}</h3>
              <ul className="mt-6 space-y-4 text-base text-[rgb(var(--text-muted))]">
                {links.map((link) => (
                  <li key={link}>
                    <a href="#" className="transition hover:text-[rgb(var(--text-strong))]">
                      {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="mt-14 flex flex-col justify-between gap-4 border-t border-[rgb(var(--line))] pt-9 text-sm font-semibold text-[rgb(var(--text-muted))] md:flex-row">
          <p>© 1847-2026 SAFE BANK · Member FDIC · All rights reserved</p>
          <p>Regulated in 28 jurisdictions</p>
        </div>
      </div>
    </footer>
  );
}

function AuthenticationGate({
  openAuth,
  theme,
  toggleTheme,
}: {
  openAuth: (mode: AuthMode) => void;
  theme: 'dark' | 'light';
  toggleTheme: () => void;
}) {
  const ThemeIcon = theme === 'dark' ? Moon : Sun;

  return (
    <main className="pattern-bg relative flex min-h-screen items-center px-6 py-12 sm:px-10">
      <button
        type="button"
        onClick={toggleTheme}
        className="absolute right-6 top-6 grid h-10 w-10 place-items-center rounded-full border border-[rgb(var(--line-strong))] bg-[rgb(var(--float-bg))] text-[rgb(var(--gold))] shadow-vault transition hover:-translate-y-0.5 sm:right-10"
        aria-label={`Switch to ${theme === 'dark' ? 'light' : 'dark'} theme`}
      >
        <ThemeIcon size={20} strokeWidth={1.8} />
      </button>

      <section className="mx-auto w-full max-w-[680px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-8 text-center shadow-vault sm:p-10">
        <div className="flex justify-center">
          <Logo />
        </div>
        <div className="mx-auto mt-10 grid h-14 w-14 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
          <LockKeyhole size={24} strokeWidth={1.8} />
        </div>
        <p className="mt-7 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">
          Secure Access Required
        </p>
        <h1 className="mt-4 font-display text-[clamp(2.8rem,5vw,4.4rem)] font-semibold leading-none text-[rgb(var(--text-strong))]">
          Login to Continue
        </h1>
        <p className="mx-auto mt-6 max-w-[520px] text-base leading-7 text-[rgb(var(--text-muted))]">
          Sign in to access your portfolio, transfers, and private banking workspace.
        </p>
        <div className="mt-9 flex flex-col justify-center gap-3 sm:flex-row">
          <button
            type="button"
            onClick={() => openAuth('login')}
            className="rounded-md bg-[rgb(var(--gold))] px-8 py-4 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
          >
            Client Login
          </button>
          <button
            type="button"
            onClick={() => openAuth('register')}
            className="rounded-md border border-[rgb(var(--button-line))] px-8 py-4 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]"
          >
            Register
          </button>
        </div>
      </section>
    </main>
  );
}

function App() {
  const [theme, setTheme] = React.useState<'dark' | 'light'>(() => {
    if (typeof window === 'undefined') return 'dark';
    return (localStorage.getItem('nexvault-theme') as 'dark' | 'light' | null) ?? 'dark';
  });
  const [authMode, setAuthMode] = React.useState<AuthMode>('login');
  const [isAuthOpen, setIsAuthOpen] = React.useState(false);
  const [authSession, setAuthSession] = React.useState<AuthSession | null>(() => {
    if (typeof window === 'undefined') return null;
    return readStoredAuthSession();
  });
  const [oneTimeEPin, setOneTimeEPin] = React.useState<string | null>(null);
  const [page, setPage] = React.useState<PageMode>(() => {
    if (typeof window === 'undefined') return 'home';
    return getPageFromPath(window.location.pathname);
  });

  const [showExpiredToast, setShowExpiredToast] = React.useState(false);

  const openAuth = React.useCallback((mode: AuthMode) => {
    setAuthMode(mode);
    setIsAuthOpen(true);
  }, []);

  const navigateTo = React.useCallback((nextPage: PageMode) => {
    const nextPath = getPathFromPage(nextPage);
    if (window.location.pathname !== nextPath) {
      window.history.pushState({}, '', nextPath);
    }
    setPage(nextPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const showHome = React.useCallback(() => navigateTo('home'), [navigateTo]);

  const showTransactions = React.useCallback(() => {
    navigateTo('transactions');
  }, [navigateTo]);

  const showAccounts = React.useCallback(() => {
    navigateTo('accounts');
  }, [navigateTo]);

  const showProfile = React.useCallback(() => {
    navigateTo('profile');
  }, [navigateTo]);

  const showPortfolio = React.useCallback(() => {
    navigateTo('portfolio');
  }, [navigateTo]);

  const showAdmin = React.useCallback(() => {
    navigateTo('admin');
  }, [navigateTo]);

  const handleAuthenticated = React.useCallback((result: AuthenticationResult) => {
    const { oneTimeEPin: generatedEPin, ...session } = result;
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
    setAuthSession(session);
    setOneTimeEPin(generatedEPin);
  }, []);

  const handleLogout = React.useCallback(() => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setAuthSession(null);
    setOneTimeEPin(null);
    setAuthMode('login');
  }, []);

  const triggerSessionExpired = React.useCallback(() => {
    setShowExpiredToast(true);
    setTimeout(() => {
      handleLogout();
      setShowExpiredToast(false);
    }, 5000);
  }, [handleLogout]);

  React.useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('nexvault-theme', theme);
  }, [theme]);

  React.useEffect(() => {
    const handlePopState = () => {
      setPage(getPageFromPath(window.location.pathname));
      window.scrollTo({ top: 0, behavior: 'auto' });
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  React.useEffect(() => {
    if (!authSession) {
      setAuthMode('login');
      setIsAuthOpen(true);
    }
  }, [authSession]);

  React.useEffect(() => {
    if (!authSession) return;

    const exp = getJwtExpiration(authSession.accessToken);
    if (!exp) return;

    const currentTime = Math.floor(Date.now() / 1000);
    const timeUntilExpiry = exp - currentTime;

    if (timeUntilExpiry <= 0) {
      triggerSessionExpired();
    } else {
      const timerId = setTimeout(() => {
        triggerSessionExpired();
      }, timeUntilExpiry * 1000);
      return () => clearTimeout(timerId);
    }
  }, [authSession, triggerSessionExpired]);

  if (!authSession) {
    return (
      <>
        <AuthenticationGate
          openAuth={openAuth}
          theme={theme}
          toggleTheme={() => setTheme((value) => (value === 'dark' ? 'light' : 'dark'))}
        />
        <AuthPopup
          mode={authMode}
          isOpen={isAuthOpen}
          onClose={() => setIsAuthOpen(false)}
          onModeChange={setAuthMode}
          onAuthenticated={handleAuthenticated}
        />
      </>
    );
  }

  return (
    <>
      {showExpiredToast && (
        <div className="fixed bottom-6 right-6 z-[100] max-w-sm rounded-lg border border-red-500/30 bg-red-500/10 p-4 shadow-lg backdrop-blur-sm animate-in fade-in slide-in-from-bottom-5">
          <div className="flex gap-3">
            <span className="grid h-5 w-5 shrink-0 place-items-center rounded-full bg-red-500/20 text-red-500">
              <Zap size={12} strokeWidth={3} />
            </span>
            <div>
              <p className="text-sm font-bold text-red-500">Session Expired</p>
              <p className="mt-1 text-xs font-semibold text-[rgb(var(--text-muted))]">
                For your security, you will be automatically redirected to the login screen in 5 seconds.
              </p>
            </div>
          </div>
        </div>
      )}
      {oneTimeEPin && (
        <OneTimeEPinNotice
          ePin={oneTimeEPin}
          onClose={() => setOneTimeEPin(null)}
        />
      )}

      <Header
        theme={theme}
        toggleTheme={() => setTheme((value) => (value === 'dark' ? 'light' : 'dark'))}
        openAuth={openAuth}
        authSession={authSession}
        onLogout={handleLogout}
        showAdmin={showAdmin}
        showHome={showHome}
        showAccounts={showAccounts}
        showProfile={showProfile}
        showTransactions={showTransactions}
        showPortfolio={showPortfolio}
      />
      <main>
        {page === 'home' && (
          <>
            <Hero showTransactions={showTransactions} showPortfolio={showPortfolio} />
            <StatsBand />
            <Cta showTransactions={showTransactions} showPortfolio={showPortfolio} />
          </>
        )}
        {page === 'accounts' && authSession && <AccountsPage authSession={authSession} showHome={showHome} showTransactions={showTransactions} />}
        {page === 'profile' && <UserPage authSession={authSession} showHome={showHome} showAccounts={showAccounts} />}
        {page === 'transactions' && <TransactionsPage authSession={authSession} showHome={showHome} />}
        {page === 'portfolio' && <PortfolioPage authSession={authSession} showHome={showHome} showTransactions={showTransactions} />}
        {page === 'admin' && <AdminRoute authSession={authSession} openAuth={openAuth} showHome={showHome} />}
      </main>
      {page === 'home' && <Footer />}
      <AuthPopup
        mode={authMode}
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onModeChange={setAuthMode}
        onAuthenticated={handleAuthenticated}
      />
    </>
  );
}
const rootElement = document.getElementById('root');
if (!rootElement) throw new Error('Failed to find the root element');

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
