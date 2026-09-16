const SITES = {
  anime: {
    title: 'Ver Anime',
    badge: 'badge-anime',
    sites: [
      { name: 'AnimeAV1', url: 'https://animeav1.com/', desc: 'Anime en español', favicon: 'favicons/animeav1.png' },
      { name: 'JKAnime', url: 'https://jkanime.net/', desc: 'Anime subbed y dubbed', favicon: 'favicons/jkanime.png' },
      { name: 'AnimeFenix', url: 'https://ww1.animefenix.vip/', desc: 'Anime online gratis', favicon: 'favicons/animefenix.png' },
      { name: 'AnimeOnlineNinja', url: 'https://ww3.animeonline.ninja/0/', desc: 'Anime online gratis', favicon: 'favicons/animeonlineninja.png' }
    ]
  },
  peliculas: {
    title: 'Películas',
    badge: 'badge-peliculas',
    sites: [
      { name: 'Runtime', url: 'https://www.runtime.tv/es/app', desc: 'Películas y series gratis', favicon: 'favicons/runtime.png' },
      { name: 'Pluto TV', url: 'https://pluto.tv/latam/home/', desc: 'TV y películas gratis', favicon: 'favicons/pluto.png' },
      { name: 'Peelink', url: 'https://www.peelink2.com/espanol', desc: 'Películas y series', favicon: 'favicons/peelink.png' }
    ]
  },
  manhwa: {
    title: 'Manhwas',
    badge: 'badge-manhwa',
    sites: [
      { name: 'ManhwaWeb', url: 'https://www.manhwaweb.com/', desc: 'Manhwas y novelas coreanas', favicon: 'favicons/manhwaweb.png' },
      { name: 'ShadeManga', url: 'https://shademanga.com/reader/M2VC85', desc: 'Manhwas en línea', favicon: 'favicons/shademanga.png' },
      { name: 'Lector Mangas', url: 'https://lector-mangas.lat/', desc: 'Manhwas y mangas', favicon: 'favicons/lectormangas.png' }
    ]
  },
  manga: {
    title: 'Mangas',
    badge: 'badge-manga',
    sites: [
      { name: 'SPN Manga', url: 'https://www.spnmanga.com/', desc: 'Mangas en español', favicon: 'favicons/spnmanga.png' },
      { name: 'MangaFire', url: 'https://mangafire.to/', desc: 'Mangas online', favicon: 'favicons/mangafire.png' },
      { name: 'Manga Million', url: 'https://mangamillion.shueisha.co.jp/es', desc: 'Mangas en español', favicon: 'favicons/mangamillion.png' }
    ]
  },
  novelas: {
    title: 'Novelas Ligeras',
    badge: 'badge-novelas',
    sites: [
      { name: 'Novelas Ligeras', url: 'https://novelasligeras.net/', desc: 'Novelas ligeras en español', favicon: 'favicons/novelasligeras.png' },
      { name: 'SkyNovels', url: 'https://www.skynovels.net/', desc: 'Novelas ligeras', favicon: 'favicons/skynovels.png' },
      { name: 'NextNovels', url: 'https://nextnovels.com/', desc: 'Novelas ligeras online', favicon: 'favicons/nextnovels.png' }
    ]
  }
};

let currentView = 'splash';
let currentCategory = null;

function isNative() {
  return !!(window.Capacitor && window.Capacitor.isNativePlatform && window.Capacitor.isNativePlatform());
}

function init() {
  const splash = document.getElementById('splash');
  SoundFX.welcome();

  setTimeout(() => {
    splash.classList.add('fade-out');
    setTimeout(() => {
      splash.classList.add('hidden');
      document.getElementById('menu').classList.remove('hidden');
      currentView = 'menu';
    }, 500);
  }, 2200);

  document.getElementById('backBtn').addEventListener('click', goBack);

  document.querySelectorAll('.card').forEach(card => {
    card.addEventListener('click', () => {
      const cat = card.dataset.category;
      if (cat) openCategory(cat);
    });
  });
}

function openCategory(category) {
  SoundFX.open();
  currentCategory = category;
  const data = SITES[category];
  if (!data) return;

  document.getElementById('selectorTitle').textContent = data.title;
  const list = document.getElementById('siteList');
  list.innerHTML = '';

  data.sites.forEach((site, i) => {
    const item = document.createElement('div');
    item.className = 'site-item';
    item.style.animationDelay = `${i * 0.06}s`;

    const faviconUrl = site.favicon;
    const initial = site.name.charAt(0).toUpperCase();
    const color = getInitialColor(site.name);

    item.innerHTML = `
      <div class="site-item-inner">
        <div class="site-favicon-container">
          <img class="site-favicon" src="${faviconUrl}" alt=""
               onerror="this.parentElement.innerHTML='<div class=\\'site-favicon-fallback\\' style=\\'background:${color}\\'>${initial}</div>'" />
        </div>
        <div class="site-item-info">
          <div class="site-item-name">${site.name}</div>
          <div class="site-item-url">${site.url}</div>
          <span class="site-item-badge ${data.badge}">${site.desc}</span>
        </div>
      </div>
    `;
    item.addEventListener('click', () => openSite(site));
    list.appendChild(item);
  });

  document.getElementById('menu').classList.add('hidden');
  document.getElementById('siteSelector').classList.remove('hidden');
  currentView = 'sites';
}

function openSite(site) {
  SoundFX.click();

  if (isNative()) {
    const q = 'url=' + encodeURIComponent(site.url) + '&title=' + encodeURIComponent(site.name);
    window.location.href = 'danganime://open?' + q;
  } else {
    window.open(site.url, '_blank');
  }
}

function getInitialColor(name) {
  const colors = ['#ff6b9d', '#c44dff', '#00d4ff', '#00ff88', '#ffc107', '#4d79ff'];
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  return colors[Math.abs(hash) % colors.length];
}

function goBack() {
  SoundFX.back();
  if (currentView === 'sites') {
    document.getElementById('siteSelector').classList.add('hidden');
    document.getElementById('menu').classList.remove('hidden');
    currentView = 'menu';
    currentCategory = null;
  }
}

function appHome() {
  SoundFX.back();
  document.getElementById('siteSelector').classList.add('hidden');
  document.getElementById('menu').classList.remove('hidden');
  currentView = 'menu';
  currentCategory = null;
}

function handleBackPress() {
  if (currentView === 'sites') {
    goBack();
    return 'handled';
  }
  if (currentView === 'menu') {
    return 'exit';
  }
  return 'no_handler';
}

document.addEventListener('DOMContentLoaded', init);
document.addEventListener('touchstart', () => SoundFX.init(), { once: true });
document.addEventListener('click', () => SoundFX.init(), { once: true });
