import '@vaadin/tooltip/theme/lumo/vaadin-tooltip.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/button/theme/lumo/vaadin-button.js';
import '@vaadin/vertical-layout/theme/lumo/vaadin-vertical-layout.js';
import 'Frontend/generated/jar-resources/disableOnClickFunctions.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';
import 'Frontend/generated/jar-resources/ReactRouterOutletElement.tsx';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '49d4575e253f82ddd79af2bc6eeffcd5d35407fcaf1638cac1d2f84abf0e7104') {
    pending.push(import('./chunks/chunk-96e1e6f2beaf6416a0a81dc7ebfb77c858fc2d3ccf955a187bb6c9c19bdf0f23.js'));
  }
  if (key === '68ee469a00a6735a00006348f6de8082cfcacee5dbaf45b3a3032328a56d994b') {
    pending.push(import('./chunks/chunk-102eaccf4d310657f8520d21ef8b220a5fbbb72c18bd29accd97dfd7b83d59c1.js'));
  }
  if (key === '3734d9c6c43c581a4877fe31a8aaf1a0413efb4d9fefd459794f0eec0c0ad5a2') {
    pending.push(import('./chunks/chunk-e9211b9736aaaa73509a790a24eb0bdaca4c445e4d63f5a307053e1324a13d63.js'));
  }
  if (key === 'edeabf76a7c6c5b1e8df639bd17017c02e6a58852107a6c39790138383d369a5') {
    pending.push(import('./chunks/chunk-76fe31e269d7aba87fc106463698bba33f053866263084e8b334f3a9e685aae1.js'));
  }
  if (key === '260df6b6f1aecfd2e7e0524e32023d33d591d3c9cede31c6c8c094d37c87fd89') {
    pending.push(import('./chunks/chunk-2e60e8c1da585d977792a4b0e7d4c188fd5c2b33b43ae35bb1c150cb021a77fd.js'));
  }
  if (key === 'e8a975481dc17dd7f39fd8e5224e8cb37f71c4f72fb7b6b310b3af983d28dc47') {
    pending.push(import('./chunks/chunk-785834d96060bb7fd4f038a87702951554433eb75cc54b243b5ebdbc88bc6849.js'));
  }
  if (key === '4795145bc7af55a5231628245e65c8c77cd440dd3398405b0ce3d007ca8793b0') {
    pending.push(import('./chunks/chunk-102eaccf4d310657f8520d21ef8b220a5fbbb72c18bd29accd97dfd7b83d59c1.js'));
  }
  if (key === '39dcfa93c130d09bc1cf350e99c36d9041ef03d22b068c9b73d782aef8b691ae') {
    pending.push(import('./chunks/chunk-102eaccf4d310657f8520d21ef8b220a5fbbb72c18bd29accd97dfd7b83d59c1.js'));
  }
  if (key === 'e776cdae2745f0de250f387620655a379e4ab4c0f504484a8c06fff8f19ed595') {
    pending.push(import('./chunks/chunk-e42efe992932324ae8b21a4c13cd766a25d5084a81fc563ce78cad05a32d10eb.js'));
  }
  if (key === '4777a48d77aa23feb91117e81a8b21f5dfbd177486a6a0bcfa876984f2dee7f5') {
    pending.push(import('./chunks/chunk-2e60e8c1da585d977792a4b0e7d4c188fd5c2b33b43ae35bb1c150cb021a77fd.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;
window.Vaadin.Flow.resetFocus = () => {
 let ae=document.activeElement;
 while(ae&&ae.shadowRoot) ae = ae.shadowRoot.activeElement;
 return !ae || ae.blur() || ae.focus() || true;
}