import React from 'react';
import ReactDOM from 'react-dom/client';
import { LockKeyhole, Mail, Moon, Sun, UserPlus, X, Zap } from 'lucide-react';
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
type PageMode = 'home' | 'transactions';

function Header({
  theme,
  toggleTheme,
  openAuth,
  showHome,
  showTransactions,
}: {
  theme: 'dark' | 'light';
  toggleTheme: () => void;
  openAuth: (mode: AuthMode) => void;
  showHome: () => void;
  showTransactions: () => void;
}) {
  const ThemeIcon = theme === 'dark' ? Moon : Sun;

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b border-[rgb(var(--line))] bg-[rgb(var(--nav-bg))]/96 backdrop-blur">
      <nav className="flex h-[60px] items-center justify-between px-6 sm:px-10">
        <Logo onClick={showHome} />
        <div className="hidden items-center gap-10 text-sm font-semibold text-[rgb(var(--text-muted))] lg:flex">
          <button type="button" onClick={showTransactions} className="transition hover:text-[rgb(var(--text-strong))]">
            Transactions
          </button>
          <button
            type="button"
            onClick={() => {
              showHome();
              window.setTimeout(() => document.getElementById('portfolio')?.scrollIntoView({ behavior: 'smooth', block: 'center' }), 0);
            }}
            className="transition hover:text-[rgb(var(--text-strong))]"
          >
            Portfolio
          </button>
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
          <button
            type="button"
            onClick={() => openAuth('login')}
            className="hidden text-sm font-semibold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))] sm:inline"
          >
            Client Login
          </button>
          <button
            type="button"
            onClick={() => openAuth('register')}
            className="rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-bold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 hover:brightness-105"
          >
            Inquire
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

function Hero({ showTransactions }: { showTransactions: () => void }) {
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
            <a href="#portfolio" className="rounded-md border border-[rgb(var(--button-line))] px-8 py-4 text-center text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]">
              Portfolio
            </a>
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

          <div className="rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault">
            <div className="flex items-center justify-between border-b border-[rgb(var(--line))] pb-5">
              <div>
                <p className="text-[0.68rem] font-extrabold uppercase tracking-[0.32em] text-[rgb(var(--gold))]">New Transfer</p>
                <h2 className="mt-2 font-display text-3xl font-semibold text-[rgb(var(--text-strong))]">Send Funds</h2>
              </div>
              <div className="grid h-11 w-11 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
                <Zap size={18} strokeWidth={1.8} />
              </div>
            </div>

            <form className="mt-6 grid gap-4" onSubmit={(event) => event.preventDefault()}>
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
                  <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">Transfer Type</span>
                  <select className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none focus:border-[rgb(var(--gold))]">
                    <option>Domestic wire</option>
                    <option>Internal transfer</option>
                    <option>International wire</option>
                  </select>
                </label>
              </div>
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

function Cta({ showTransactions }: { showTransactions: () => void }) {
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
          <a href="#portfolio" className="rounded-md border border-[rgb(var(--button-line))] px-9 py-4 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]">
            Portfolio
          </a>
        </div>
        <p className="mt-8 text-sm font-semibold text-[rgb(var(--text-muted))]">
          By invitation and introduction · FDIC insured · Serving families since 1847
        </p>
      </div>
    </section>
  );
}

function AuthPopup({
  mode,
  isOpen,
  onClose,
  onModeChange,
}: {
  mode: AuthMode;
  isOpen: boolean;
  onClose: () => void;
  onModeChange: (mode: AuthMode) => void;
}) {
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

  if (!isOpen) return null;

  const isLogin = mode === 'login';

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

        <div className="pr-10">
          <div className="grid h-12 w-12 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
            {isLogin ? <LockKeyhole size={20} strokeWidth={1.8} /> : <UserPlus size={20} strokeWidth={1.8} />}
          </div>
          <p className="mt-6 text-[0.68rem] font-extrabold uppercase tracking-[0.34em] text-[rgb(var(--gold))]">
            Secure Client Access
          </p>
          <h2 id="auth-title" className="mt-3 font-display text-4xl font-semibold leading-tight text-[rgb(var(--text-strong))]">
            {isLogin ? 'Welcome Back' : 'Request Access'}
          </h2>
          <p className="mt-3 text-sm leading-6 text-[rgb(var(--text-muted))]">
            {isLogin
              ? 'Sign in to review your portfolio, advisory notes, and private banking activity.'
              : 'Create an access request and a private banking advisor will review your introduction.'}
          </p>
        </div>

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

        <form
          className="mt-7 space-y-4"
          onSubmit={(event) => {
            event.preventDefault();
            onClose();
          }}
        >
          {!isLogin && (
            <label className="block">
              <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
                Full Name
              </span>
              <input
                type="text"
                autoComplete="name"
                className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                placeholder="Alex Morgan"
                required
              />
            </label>
          )}

          <label className="block">
            <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
              Email Address
            </span>
            <div className="relative">
              <Mail className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-[rgb(var(--text-muted))]" size={16} />
              <input
                type="email"
                autoComplete="email"
                className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] py-3 pl-11 pr-4 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                placeholder="client@example.com"
                required
              />
            </div>
          </label>

          <label className="block">
            <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
              Password
            </span>
            <input
              type="password"
              autoComplete={isLogin ? 'current-password' : 'new-password'}
              className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
              placeholder={isLogin ? 'Enter your password' : 'Create a password'}
              required
            />
          </label>

          {!isLogin && (
            <label className="block">
              <span className="mb-2 block text-xs font-extrabold uppercase tracking-[0.18em] text-[rgb(var(--text-muted))]">
                Date of Birth
              </span>
              <input
                type="text"
                autoComplete="off"
                className="w-full rounded-md border border-[rgb(var(--line))] bg-[rgb(var(--page-bg))] px-4 py-3 text-sm font-semibold text-[rgb(var(--text-strong))] outline-none transition placeholder:text-[rgb(var(--text-muted))]/70 focus:border-[rgb(var(--gold))]"
                placeholder="DD.MM.YYYY"
                required
              />
            </label>
          )}

          <button
            type="submit"
            className="w-full rounded-md bg-[rgb(var(--gold))] px-6 py-3.5 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 hover:brightness-105"
          >
            {isLogin ? 'Login Securely' : 'Register Interest'}
          </button>
        </form>

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

function App() {
  const [theme, setTheme] = React.useState<'dark' | 'light'>(() => {
    if (typeof window === 'undefined') return 'dark';
    return (localStorage.getItem('nexvault-theme') as 'dark' | 'light' | null) ?? 'dark';
  });
  const [authMode, setAuthMode] = React.useState<AuthMode>('login');
  const [isAuthOpen, setIsAuthOpen] = React.useState(false);
  const [page, setPage] = React.useState<PageMode>('home');

  const openAuth = React.useCallback((mode: AuthMode) => {
    setAuthMode(mode);
    setIsAuthOpen(true);
  }, []);

  const showHome = React.useCallback(() => {
    setPage('home');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  const showTransactions = React.useCallback(() => {
    setPage('transactions');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  React.useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('nexvault-theme', theme);
  }, [theme]);

  return (
    <>
      <Header
        theme={theme}
        toggleTheme={() => setTheme((value) => (value === 'dark' ? 'light' : 'dark'))}
        openAuth={openAuth}
        showHome={showHome}
        showTransactions={showTransactions}
      />
      <main>
        {page === 'home' ? (
          <>
            <Hero showTransactions={showTransactions} />
            <StatsBand />
            <Cta showTransactions={showTransactions} />
          </>
        ) : (
          <TransactionsPage showHome={showHome} />
        )}
      </main>
      {page === 'home' && <Footer />}
      <AuthPopup
        mode={authMode}
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onModeChange={setAuthMode}
      />
    </>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
