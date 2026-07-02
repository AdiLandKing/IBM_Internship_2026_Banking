import React from 'react';
import ReactDOM from 'react-dom/client';
import { BarChart3, BriefcaseBusiness, Globe2, Moon, Shield, Sun, TrendingUp, Vault, Zap } from 'lucide-react';
import './styles.css';

const stats = [
  { value: '$2.4T', label: 'Assets Under Stewardship' },
  { value: '12,000+', label: 'Distinguished Families' },
  { value: 'Since 1847', label: 'Heritage of Excellence' },
  { value: '180', label: 'Countries Served' },
];

const services = [
  {
    icon: Shield,
    title: 'Private Vaults',
    body: 'Discretionary wealth preservation through multi-jurisdictional architecture and heritage-grade security protocols.',
  },
  {
    icon: TrendingUp,
    title: 'Portfolio Management',
    body: 'Bespoke investment strategies curated by seasoned advisors with decades of market expertise.',
  },
  {
    icon: Globe2,
    title: 'Global Banking',
    body: 'Seamless international transactions with the discretion and service excellence your family deserves.',
  },
  {
    icon: Zap,
    title: 'Credit Lines',
    body: 'Flexible lending solutions backed by your portfolio, tailored to your unique financial position.',
  },
  {
    icon: BarChart3,
    title: 'Wealth Advisory',
    body: 'Generational wealth planning with tax optimization and legacy structuring for enduring prosperity.',
  },
  {
    icon: BriefcaseBusiness,
    title: 'Concierge Security',
    body: 'White-glove identity protection and authentication services befitting your distinguished position.',
  },
];

function Logo() {
  return (
    <a href="#" className="flex items-center gap-3" aria-label="NEXVAULT home">
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

function Header({ theme, toggleTheme }: { theme: 'dark' | 'light'; toggleTheme: () => void }) {
  const ThemeIcon = theme === 'dark' ? Moon : Sun;

  return (
    <header className="fixed inset-x-0 top-0 z-50 border-b border-[rgb(var(--line))] bg-[rgb(var(--nav-bg))]/96 backdrop-blur">
      <nav className="flex h-[60px] items-center justify-between px-6 sm:px-10">
        <Logo />
        <div className="hidden items-center gap-10 text-sm font-semibold text-[rgb(var(--text-muted))] lg:flex">
          <a className="transition hover:text-[rgb(var(--text-strong))]" href="#services">
            Services
          </a>
          <a className="transition hover:text-[rgb(var(--text-strong))]" href="#advisory">
            Wealth Advisory
          </a>
          <a className="transition hover:text-[rgb(var(--text-strong))]" href="#heritage">
            Our Heritage
          </a>
          <a className="transition hover:text-[rgb(var(--text-strong))]" href="#contact">
            Contact
          </a>
        </div>
        <div className="flex items-center gap-5">
          <a className="hidden text-sm font-semibold text-[rgb(var(--text-muted))] transition hover:text-[rgb(var(--text-strong))] sm:inline" href="#login">
            Client Login
          </a>
          <a className="rounded-md bg-[rgb(var(--gold))] px-6 py-3 text-sm font-bold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5 hover:brightness-105" href="#contact">
            Inquire
          </a>
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
    <aside className="w-full max-w-[422px] rounded-lg border border-[rgb(var(--card-line))] bg-[rgb(var(--card-bg))] p-6 shadow-vault">
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

function Hero() {
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
            <a href="#contact" className="rounded-md bg-[rgb(var(--gold))] px-8 py-4 text-center text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5">
              Schedule Consultation
            </a>
            <a href="#heritage" className="rounded-md border border-[rgb(var(--button-line))] px-8 py-4 text-center text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]">
              Our Heritage
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

function Services() {
  return (
    <section id="services" className="bg-[rgb(var(--page-bg))] px-6 py-24 sm:px-10">
      <div className="mx-auto max-w-[980px]">
        <div className="mx-auto max-w-[780px] text-center">
          <Eyebrow>Our Services</Eyebrow>
          <h2 className="mt-8 font-display text-[clamp(2.75rem,3vw,3.5rem)] font-semibold leading-tight text-[rgb(var(--text-strong))]">
            Tailored to Your Legacy
          </h2>
          <p className="mx-auto mt-6 max-w-[760px] text-base leading-7 text-[rgb(var(--text-muted))]">
            Each family's wealth journey is unique. Our bespoke services are crafted to preserve, grow, and seamlessly transfer your prosperity across generations.
          </p>
        </div>
        <div className="mt-20 grid overflow-hidden border-[rgb(var(--grid-line))] md:grid-cols-2 lg:grid-cols-3">
          {services.map((service, index) => {
            const Icon = service.icon;
            return (
              <article
                key={service.title}
                className={`min-h-[210px] border-[rgb(var(--grid-line))] p-6 transition duration-300 hover:bg-[rgb(var(--service-hover))] ${
                  service.featured ? 'bg-[rgb(var(--service-featured))]' : ''
                } ${index % 3 !== 0 ? 'lg:border-l' : ''} ${index > 2 ? 'lg:border-t' : ''} ${index % 2 !== 0 ? 'md:border-l lg:border-l' : ''} ${index > 1 ? 'md:border-t lg:border-t-0' : ''}`}
              >
                <div className="grid h-9 w-9 place-items-center rounded-full border border-[rgb(var(--gold))]/35 bg-[rgb(var(--icon-bg))] text-[rgb(var(--gold))]">
                  <Icon size={15} strokeWidth={1.8} />
                </div>
                <h3 className="mt-5 font-display text-[1.22rem] font-bold text-[rgb(var(--text-strong))]">{service.title}</h3>
                <p className="mt-3 max-w-[300px] text-[0.82rem] leading-5 text-[rgb(var(--text-muted))]">{service.body}</p>
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function Cta() {
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
          <a href="#contact" className="rounded-md bg-[rgb(var(--gold))] px-9 py-4 text-sm font-extrabold text-[rgb(var(--gold-ink))] shadow-gold transition hover:-translate-y-0.5">
            Request Introduction
          </a>
          <a href="#services" className="rounded-md border border-[rgb(var(--button-line))] px-9 py-4 text-sm font-extrabold text-[rgb(var(--text-strong))] transition hover:-translate-y-0.5 hover:border-[rgb(var(--gold))]">
            Explore Services
          </a>
        </div>
        <p className="mt-8 text-sm font-semibold text-[rgb(var(--text-muted))]">
          By invitation and introduction · FDIC insured · Serving families since 1847
        </p>
      </div>
    </section>
  );
}

function Footer() {
  const groups = [
    ['Services', 'Private Banking', 'Wealth Management', 'Estate Planning', 'Concierge'],
    ['About', 'Our Heritage', 'Leadership', 'Locations', 'Insights'],
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

  React.useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem('nexvault-theme', theme);
  }, [theme]);

  return (
    <>
      <Header theme={theme} toggleTheme={() => setTheme((value) => (value === 'dark' ? 'light' : 'dark'))} />
      <main>
        <Hero />
        <StatsBand />
        <Services />
        <Cta />
      </main>
      <Footer />
    </>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
