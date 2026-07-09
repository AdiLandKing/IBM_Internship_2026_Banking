import React from 'react';
import ReactDOM from 'react-dom/client';
import { Activity, ArrowLeft, Calendar, Check, ChartPie, Copy, CreditCard, Eye, EyeOff, Landmark, LockKeyhole, Mail, Moon, Pencil, Plus, Search, Settings, ShieldCheck, Sun, UserPlus, Users, Wallet, X, Zap } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import './styles.css';

const stats = [
  { value: '$2.4T', label: 'Assets Under Stewardship' },
  { value: '12,000+', label: 'Distinguished Families' },
  { value: 'Since 1847', label: 'Heritage of Excellence' },
  { value: '180', label: 'Countries Served' },
];

const recentTransactions = [
  { name: 'Hamilton Family Office', type: 'Wire transfer', amount: '-$48,250.00', date: 'Jul 02', status: 'Completed' },
  { name: 'Treasury Income Sweep', type: 'Portfolio credit', amount: '+$12,940.18', date: 'Jul 01', status: 'Settled' },
  { name: 'Zurich Custody Account', type: 'International transfer', amount: '-$86,000.00', date: 'Jun 28', status: 'Reviewed' },
  { name: 'SAFE Bank Reserve', type: 'Internal transfer', amount: '+$250,000.00', date: 'Jun 25', status: 'Completed' },
];

const holdings = [
  { name: 'Global Equity Mandate', category: 'Equities', value: '$2.51M', allocation: '52%', change: '+3.1%' },
  { name: 'Municipal Income Ladder', category: 'Fixed Income', value: '$1.35M', allocation: '28%', change: '+0.8%' },
  { name: 'Private Alternatives Fund', category: 'Alternatives', value: '$964K', allocation: '20%', change: '+1.6%' },
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

const adminUsers = [
  { name: 'Elena Hamilton', email: 'elena@hamilton-office.com', role: 'Client', status: 'Active', lastSeen: 'Today' },
  { name: 'Marcus Mori', email: 'marcus@mori-capital.com', role: 'Client', status: 'Pending KYC', lastSeen: 'Yesterday' },
  { name: 'Nadia Ainsley', email: 'nadia@ainsley.co', role: 'Advisor', status: 'Active', lastSeen: '12 min ago' },
  { name: 'Theo Grant', email: 'theo.grant@safebank.com', role: 'Admin', status: 'Active', lastSeen: '3 min ago' },
];

const transferLogs = [
  { id: 'TRF-90421', user: 'Hamilton Family Office', amount: '$48,250.00', type: 'Domestic wire', status: 'Completed', date: 'Jul 02, 2026' },
  { id: 'TRF-90403', user: 'Zurich Custody Account', amount: '$86,000.00', type: 'International wire', status: 'Reviewed', date: 'Jun 28, 2026' },
  { id: 'TRF-90377', user: 'SAFE Bank Reserve', amount: '$250,000.00', type: 'Internal transfer', status: 'Completed', date: 'Jun 25, 2026' },
  { id: 'TRF-90312', user: 'Mori Capital Trust', amount: '$19,430.00', type: 'Portfolio funding', status: 'Flagged', date: 'Jun 22, 2026' },
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
  opened: string;
  branch: string;
};

const clientAccounts: ClientAccount[] = [
  {
    id: 'private-checking',
    name: 'Private Checking',
    type: 'Everyday account',
    iban: 'BG80SAFE18470000394401',
    balance: 128420.86,
    currency: 'USD',
    opened: 'March 14, 2021',
    branch: 'Sofia Private Office',
  },
  {
    id: 'wealth-reserve',
    name: 'Wealth Reserve',
    type: 'Savings account',
    iban: 'BG31SAFE18470000184702',
    balance: 740000,
    currency: 'USD',
    opened: 'September 02, 2018',
    branch: 'Sofia Private Office',
  },
  {
    id: 'travel-fx',
    name: 'Travel & FX',
    type: 'Foreign currency account',
    iban: 'BG59SAFE18470000621803',
    balance: 42890.4,
    currency: 'EUR',
    opened: 'January 18, 2024',
    branch: 'International Banking',
  },
];

function getPageFromPath(pathname: string): PageMode {
  if (pathname === '/admin') return 'admin';
  if (pathname === '/accounts') return 'accounts';
  if (pathname === '/portfolio') return 'portfolio';
  if (pathname === '/transactions') return 'transactions';
  return 'home';
}

function getPathFromPage(page: PageMode) {
  if (page === 'admin') return '/admin';
  if (page === 'accounts') return '/accounts';
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
type PageMode = 'home' | 'accounts' | 'transactions' | 'portfolio' | 'admin';

const DEFAULT_API_BASE_URL = typeof window !== 'undefined' && window.location.port !== '5173'
  ? window.location.origin
  : 'http://127.0.0.1:8080';
const API_BASE_URL = ((import.meta as unknown as { env?: Record<string, string | undefined> }).env?.VITE_API_BASE_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, '');
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

type ApiErrorResponse = {
  message?: string;
  fieldErrors?: Record<string, string>;
};

type AuthFieldErrors = Partial<Record<'email' | 'firstName' | 'lastName' | 'dateOfBirth' | 'password' | 'confirmPassword', string>>;

const AUTH_RULE_MESSAGE = 'Password must be at least 10 characters and include 1 uppercase letter, 1 number, and 1 special character.';
const REGISTRATION_AUTH_PATTERN = String.raw`^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{10,}$`;
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

async function authenticate(mode: AuthMode, payload: Record<string, string>): Promise<AuthSession> {
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
              <span className="text-sm font-semibold text-[rgb(var(--text-muted))]">
                {authSession.user.firstName} {authSession.user.lastName}
              </span>
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

function maskIban(iban: string) {
  if (iban === 'Pending assignment') return iban;
  return `${iban.slice(0, 4)} •••• •••• ${iban.slice(-4)}`;
}

function AccountsPage({
  showHome,
  showTransactions,
}: {
  showHome: () => void;
  showTransactions: () => void;
}) {
  const [accounts, setAccounts] = React.useState<ClientAccount[]>(clientAccounts);
  const [selectedAccountId, setSelectedAccountId] = React.useState(clientAccounts[0].id);
  const [accountNames, setAccountNames] = React.useState<Record<string, string>>(
    () => Object.fromEntries(clientAccounts.map((account) => [account.id, account.name])),
  );
  const [isCreateAccountOpen, setIsCreateAccountOpen] = React.useState(false);
  const [editingName, setEditingName] = React.useState(false);
  const [draftName, setDraftName] = React.useState('');
  const [isIbanVisible, setIsIbanVisible] = React.useState(false);
  const [copiedAccountId, setCopiedAccountId] = React.useState<string | null>(null);

  const selectedAccount = accounts.find((account) => account.id === selectedAccountId) ?? accounts[0];
  const selectedName = accountNames[selectedAccount.id];
  const IbanVisibilityIcon = isIbanVisible ? EyeOff : Eye;

  function selectAccount(account: ClientAccount) {
    setSelectedAccountId(account.id);
    setEditingName(false);
    setIsIbanVisible(false);
  }

  function startRenaming() {
    setDraftName(selectedName);
    setEditingName(true);
  }

  function saveAccountName(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextName = draftName.trim();
    if (!nextName) return;
    setAccountNames((current) => ({ ...current, [selectedAccount.id]: nextName }));
    setEditingName(false);
  }

  async function copyIban() {
    await navigator.clipboard.writeText(selectedAccount.iban);
    setCopiedAccountId(selectedAccount.id);
    window.setTimeout(() => setCopiedAccountId(null), 1800);
  }

  function createAccount(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    const accountName = getFormString(formData, 'accountName', true);
    const accountType = getFormString(formData, 'accountType');
    const currency = getFormString(formData, 'currency');
    if (!accountName || !accountType || !currency) return;

    const accountId = `new-account-${Date.now()}`;
    const newAccount: ClientAccount = {
      id: accountId,
      name: accountName,
      type: accountType,
      iban: 'Pending assignment',
      balance: 0,
      currency,
      opened: new Intl.DateTimeFormat('en-US', { dateStyle: 'long' }).format(new Date()),
      branch: 'Sofia Private Office',
    };

    setAccounts((current) => [...current, newAccount]);
    setAccountNames((current) => ({ ...current, [accountId]: accountName }));
    setSelectedAccountId(accountId);
    setIsCreateAccountOpen(false);
    setEditingName(false);
    setIsIbanVisible(false);
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

        <div className="mt-10 grid gap-8 lg:grid-cols-[360px_1fr]">
          <div className="overflow-hidden rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))]">
            <div className="border-b border-[rgb(var(--line))] px-5 py-4">
              <p className="text-[0.64rem] font-extrabold uppercase tracking-[0.28em] text-[rgb(var(--text-muted))]">Account Directory</p>
            </div>
            <div className="divide-y divide-[rgb(var(--line))]">
              {accounts.map((account) => {
                const isSelected = account.id === selectedAccount.id;
                return (
                  <button
                    key={account.id}
                    type="button"
                    onClick={() => selectAccount(account)}
                    className={`w-full px-5 py-5 text-left transition ${
                      isSelected
                        ? 'bg-[rgb(var(--icon-bg))]'
                        : 'hover:bg-[rgb(var(--service-hover))]'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="min-w-0">
                        <p className="truncate font-bold text-[rgb(var(--text-strong))]">{accountNames[account.id]}</p>
                        <p className="mt-1 text-xs font-semibold text-[rgb(var(--text-muted))]">{account.type}</p>
                      </div>
                      <CreditCard
                        size={18}
                        strokeWidth={1.7}
                        className={isSelected ? 'text-[rgb(var(--gold))]' : 'text-[rgb(var(--text-muted))]'}
                      />
                    </div>
                    <p className="mt-5 font-display text-2xl font-bold text-[rgb(var(--text-strong))]">{formatAccountBalance(account)}</p>
                    <p className="mt-2 text-xs font-semibold tracking-[0.08em] text-[rgb(var(--text-muted))]">{maskIban(account.iban)}</p>
                  </button>
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
                      maxLength={60}
                      autoFocus
                      aria-label="Account name"
                      className="min-w-0 rounded-md border border-[rgb(var(--gold))] bg-[rgb(var(--page-bg))] px-4 py-2.5 text-lg font-bold text-[rgb(var(--text-strong))] outline-none"
                    />
                    <div className="flex gap-2">
                      <button type="submit" className="grid h-11 w-11 place-items-center rounded-md bg-[rgb(var(--gold))] text-[rgb(var(--gold-ink))]" aria-label="Save account name">
                        <Check size={17} strokeWidth={2} />
                      </button>
                      <button type="button" onClick={() => setEditingName(false)} className="grid h-11 w-11 place-items-center rounded-md border border-[rgb(var(--line))] text-[rgb(var(--text-muted))]" aria-label="Cancel account name edit">
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
                {selectedAccount.iban !== 'Pending assignment' && (
                  <>
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
                  </>
                )}
              </div>
            </div>

            <dl className="grid gap-px overflow-hidden rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--line))] sm:grid-cols-2">
              {[
                ['Currency', selectedAccount.currency],
                ['Account holder', 'Primary client'],
                ['Opened', selectedAccount.opened],
                ['Servicing branch', selectedAccount.branch],
              ].map(([label, value]) => (
                <div key={label} className="bg-[rgb(var(--page-bg))] px-5 py-4">
                  <dt className="text-[0.6rem] font-extrabold uppercase tracking-[0.22em] text-[rgb(var(--text-muted))]">{label}</dt>
                  <dd className="mt-2 text-sm font-bold text-[rgb(var(--text-strong))]">{value}</dd>
                </div>
              ))}
            </dl>

            <div className="mt-7 flex flex-col gap-3 sm:flex-row">
              <button
                type="button"
                onClick={showTransactions}
                className="inline-flex items-center justify-center gap-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
              >
                <Zap size={16} strokeWidth={1.8} />
                Transfer from this account
              </button>
              <button
                type="button"
                onClick={startRenaming}
                className="inline-flex items-center justify-center gap-2 rounded-md border border-[rgb(var(--button-line))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:border-[rgb(var(--gold))]"
              >
                <Pencil size={16} strokeWidth={1.8} />
                Edit account name
              </button>
            </div>
          </article>
        </div>
      </div>

      {isCreateAccountOpen && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center px-5 py-8">
          <button
            type="button"
            aria-label="Close create account popup"
            className="absolute inset-0 bg-black/65 backdrop-blur-sm"
            onClick={() => setIsCreateAccountOpen(false)}
          />
          <section
            role="dialog"
            aria-modal="true"
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
                  maxLength={60}
                  autoFocus
                  className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                  placeholder="My savings"
                  required
                />
              </label>
              <label className="block">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Account Type</span>
                <select
                  name="accountType"
                  className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]"
                  required
                >
                  <option value="Everyday account">Everyday account</option>
                  <option value="Savings account">Savings account</option>
                  <option value="Foreign currency account">Foreign currency account</option>
                </select>
              </label>
              <label className="block">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Currency</span>
                <select
                  name="currency"
                  className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]"
                  required
                >
                  <option value="BGN">BGN</option>
                  <option value="EUR">EUR</option>
                  <option value="USD">USD</option>
                  <option value="GBP">GBP</option>
                </select>
              </label>
              <button
                type="submit"
                className="w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5"
              >
                Create account
              </button>
            </form>
          </section>
        </div>
      )}
    </section>
  );
}

function TransactionsPage({ showHome }: { showHome: () => void }) {
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
                ['Available Balance', '$4,821,390.44'],
                ['Pending Review', '$86,000.00'],
                ['Daily Limit', '$500,000.00'],
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

            <form className="mt-6 flex flex-1 flex-col gap-4" onSubmit={(event) => event.preventDefault()}>
              <label>
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">From Account</span>
                <select className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]">
                  <option>SAFE Private Checking • 3944</option>
                  <option>SAFE Reserve Account • 1847</option>
                </select>
              </label>
              <label>
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Recipient</span>
                <input className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]" placeholder="IBAN" />
              </label>
              <div className="grid gap-4 sm:grid-cols-2">
                <label>
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Amount</span>
                  <input className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]" placeholder="$0.00" />
                </label>
                <label>
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Currency</span>
                  <select className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]">
                    <option>Euro</option>
                    <option>USD</option>
                    <option>Pound</option>
                  </select>
                </label>
              </div>
              <label className="flex flex-1 flex-col">
                <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Reason</span>
                <textarea
                  className="min-h-[150px] flex-1 resize-none rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold leading-6 text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                  placeholder="Payment reference or transfer purpose"
                />
              </label>
              <button className="mt-2 rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5" type="submit">
                Review Transfer
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
            <span className="text-sm font-bold text-[rgb(var(--text-muted))]">Last 30 days</span>
          </div>
          <div className="divide-y divide-[rgb(var(--line))]">
            {recentTransactions.map((transaction) => (
              <div key={`${transaction.name}-${transaction.date}`} className="grid gap-3 py-4 sm:grid-cols-[1fr_auto_auto] sm:items-center">
                <div>
                  <p className="font-bold text-[rgb(var(--text-strong))]">{transaction.name}</p>
                  <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{transaction.type} · {transaction.date}</p>
                </div>
                <span className="text-sm font-extrabold text-[rgb(var(--gold))]">{transaction.status}</span>
                <span className={`font-display text-xl font-bold ${transaction.amount.startsWith('+') ? 'text-emerald-500' : 'text-[rgb(var(--text-strong))]'}`}>
                  {transaction.amount}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function PortfolioPage({ showHome, showTransactions }: { showHome: () => void; showTransactions: () => void }) {
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
              Review your allocation, performance, and holdings across the mandates managed by SAFE Bank advisors.
            </p>
            <div className="mt-10 grid gap-4 sm:grid-cols-3">
              {[
                ['Total Value', '$4,821,390.44'],
                ['YTD Return', '+11.8%'],
                ['Risk Profile', 'Balanced'],
              ].map(([label, value]) => (
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
              {holdings.map((holding) => (
                <div key={holding.category}>
                  <div className="mb-2 flex items-center justify-between text-sm font-bold">
                    <span className="text-[rgb(var(--text-strong))]">{holding.category}</span>
                    <span className="text-[rgb(var(--gold))]">{holding.allocation}</span>
                  </div>
                  <div className="h-2 rounded-full bg-[rgb(var(--line))]">
                    <div className="h-full rounded-full bg-[rgb(var(--gold))]" style={{ width: holding.allocation }} />
                  </div>
                </div>
              ))}
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
          <div className="divide-y divide-[rgb(var(--line))]">
            {holdings.map((holding) => (
              <div key={holding.name} className="grid gap-3 py-5 sm:grid-cols-[1.1fr_0.7fr_auto_auto] sm:items-center">
                <div>
                  <p className="font-bold text-[rgb(var(--text-strong))]">{holding.name}</p>
                  <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{holding.category}</p>
                </div>
                <span className="text-sm font-extrabold text-[rgb(var(--text-muted))]">{holding.allocation} allocation</span>
                <span className="font-display text-xl font-bold text-[rgb(var(--text-strong))]">{holding.value}</span>
                <span className="text-sm font-extrabold text-emerald-500">{holding.change}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function AdminPage({ showHome }: { showHome: () => void }) {
  const [activeMenu, setActiveMenu] = React.useState<AdminMenuId>('pending');
  const activeMenuDetails = adminMenus.find((menu) => menu.id === activeMenu) ?? adminMenus[0];

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
                Frontend Demo
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
                    <span
                      className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${
                        item.priority === 'High'
                          ? 'bg-red-500/15 text-red-500'
                          : item.priority === 'Medium'
                            ? 'bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]'
                            : 'bg-emerald-500/15 text-emerald-500'
                      }`}
                    >
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
              <div>
                <label className="mb-5 block">
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Search Users</span>
                  <input
                    className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                    placeholder="Search by name, email, role, or status"
                  />
                </label>
                <div className="divide-y divide-[rgb(var(--line))]">
                  {adminUsers.map((user) => (
                    <div key={user.email} className="grid gap-3 py-4 lg:grid-cols-[1fr_auto_auto] lg:items-center">
                      <div>
                        <p className="font-bold text-[rgb(var(--text-strong))]">{user.name}</p>
                        <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{user.email} · Last seen {user.lastSeen}</p>
                      </div>
                      <span className="text-sm font-extrabold text-[rgb(var(--gold))]">{user.role}</span>
                      <span className="w-fit rounded-full bg-emerald-500/15 px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] text-emerald-500">
                        {user.status}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {activeMenu === 'logs' && (
              <div>
                <div className="mb-5 grid gap-3 sm:grid-cols-3">
                  <input className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]" placeholder="Transfer ID" />
                  <input className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]" placeholder="User or account" />
                  <select className="rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]">
                    <option>All statuses</option>
                    <option>Completed</option>
                    <option>Reviewed</option>
                    <option>Flagged</option>
                  </select>
                </div>
                <div className="divide-y divide-[rgb(var(--line))]">
                  {transferLogs.map((log) => (
                    <div key={log.id} className="grid gap-3 py-4 xl:grid-cols-[0.7fr_1fr_auto_auto] xl:items-center">
                      <span className="font-mono text-sm font-bold text-[rgb(var(--gold))]">{log.id}</span>
                      <div>
                        <p className="font-bold text-[rgb(var(--text-strong))]">{log.user}</p>
                        <p className="mt-1 text-sm font-semibold text-[rgb(var(--text-muted))]">{log.type} · {log.date}</p>
                      </div>
                      <span className="font-display text-xl font-bold text-[rgb(var(--text-strong))]">{log.amount}</span>
                      <span className={`w-fit rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.16em] ${log.status === 'Flagged' ? 'bg-red-500/15 text-red-500' : 'bg-emerald-500/15 text-emerald-500'}`}>
                        {log.status}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
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

  if (!isAllowed) {
    return <AdminAccessGate authSession={authSession} openAuth={openAuth} showHome={showHome} />;
  }

  return <AdminPage showHome={showHome} />;
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
  onAuthenticated: (session: AuthSession) => void;
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
        <p className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm font-bold text-emerald-500" role="status">
          {success}
        </p>
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
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-title"
        className="relative w-full max-w-[460px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-[0_28px_90px_rgba(0,0,0,0.45)] sm:p-8"
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
      </section>
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

  const showPortfolio = React.useCallback(() => {
    navigateTo('portfolio');
  }, [navigateTo]);

  const showAdmin = React.useCallback(() => {
    navigateTo('admin');
  }, [navigateTo]);

  const handleAuthenticated = React.useCallback((session: AuthSession) => {
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
    setAuthSession(session);
  }, []);

  const handleLogout = React.useCallback(() => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setAuthSession(null);
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

      <Header
        theme={theme}
        toggleTheme={() => setTheme((value) => (value === 'dark' ? 'light' : 'dark'))}
        openAuth={openAuth}
        authSession={authSession}
        onLogout={handleLogout}
        showAdmin={showAdmin}
        showHome={showHome}
        showAccounts={showAccounts}
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
        {page === 'accounts' && <AccountsPage showHome={showHome} showTransactions={showTransactions} />}
        {page === 'transactions' && <TransactionsPage showHome={showHome} />}
        {page === 'portfolio' && <PortfolioPage showHome={showHome} showTransactions={showTransactions} />}
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