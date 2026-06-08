/* ======================================
   SARVASVA NATURALS – MAIN JS
   ====================================== */

document.addEventListener('DOMContentLoaded', () => {
  initNavbar();
  initSearch();
  initMobileMenu();
  initCartButtons();
  initFlashMessages();
  initWeightSelector();
  initQuantityControls();
  initCartActions();
  initAddressCards();
  initPaymentOptions();
  initCoupon();
  initGallery();
  initScrollAnimations();
});

// ===== NAVBAR SCROLL =====
function initNavbar() {
  const navbar = document.getElementById('navbar');
  if (!navbar) return;
  window.addEventListener('scroll', () => {
    navbar.classList.toggle('scrolled', window.scrollY > 50);
  }, { passive: true });
}

// ===== SEARCH TOGGLE =====
function initSearch() {
  const toggle = document.getElementById('searchToggle');
  const bar    = document.getElementById('searchBar');
  const close  = document.getElementById('searchClose');
  if (!toggle || !bar) return;

  toggle.addEventListener('click', () => {
    bar.classList.add('open');
    bar.querySelector('input')?.focus();
  });
  close?.addEventListener('click', () => bar.classList.remove('open'));
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') bar.classList.remove('open');
  });
}

// ===== MOBILE MENU =====
function initMobileMenu() {
  const hamburger = document.getElementById('hamburger');
  const navLinks  = document.getElementById('navLinks');
  if (!hamburger || !navLinks) return;

  hamburger.addEventListener('click', () => {
    hamburger.classList.toggle('open');
    navLinks.classList.toggle('mobile-open');
  });
}

// ===== ADD TO CART =====
function initCartButtons() {
  document.querySelectorAll('.add-to-cart-btn, .add-to-cart-large').forEach(btn => {
    btn.addEventListener('click', async (e) => {
      e.preventDefault();
      const productId = btn.dataset.productId;
      if (!productId) return;

      const weightEl = document.querySelector('.weight-btn.active');
      const weight   = weightEl ? weightEl.dataset.weight : null;
      const qtyInput = document.getElementById('detailQty');
      const quantity = qtyInput ? parseInt(qtyInput.value) : 1;

      btn.classList.add('loading');
      const originalHTML = btn.innerHTML;
      btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding...';

      try {
        const res = await fetch('/api/cart/add', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ productId, quantity, weight })
        });
        const data = await res.json();
        if (data.success) {
          updateCartCount(data.cartCount);
          showCartToast();
        } else {
          showNotification(data.message || 'Failed to add to cart', 'error');
        }
      } catch (err) {
        showNotification('Something went wrong. Please try again.', 'error');
      } finally {
        btn.classList.remove('loading');
        btn.innerHTML = originalHTML;
      }
    });
  });
}

// ===== CART COUNT =====
function updateCartCount(count) {
  const el = document.getElementById('cartCount');
  if (!el) return;
  el.textContent = count;
  el.style.display = count > 0 ? 'flex' : 'none';
  el.classList.add('bounce');
  setTimeout(() => el.classList.remove('bounce'), 400);
}

// ===== CART TOAST =====
function showCartToast() {
  const toast = document.getElementById('cartToast');
  if (!toast) return;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3500);
}

// ===== FLASH MESSAGES =====
function initFlashMessages() {
  document.querySelectorAll('.flash-close').forEach(btn => {
    btn.addEventListener('click', () => {
      btn.closest('.flash-message')?.remove();
    });
  });
  // Auto dismiss after 5s
  document.querySelectorAll('.flash-message').forEach(msg => {
    setTimeout(() => {
      msg.style.opacity = '0';
      msg.style.transform = 'translateX(120%)';
      setTimeout(() => msg.remove(), 300);
    }, 5000);
  });
}

// ===== WEIGHT SELECTOR =====
function initWeightSelector() {
  document.querySelectorAll('.weight-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.weight-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      // Update price if needed
      const price = btn.dataset.price;
      if (price) {
        const priceEl = document.querySelector('.product-detail-price');
        if (priceEl) priceEl.textContent = '₹' + price;
      }
    });
  });
}

// ===== QUANTITY CONTROLS =====
function initQuantityControls() {
  document.querySelectorAll('.qty-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const input = btn.closest('.qty-control')?.querySelector('.qty-input, input[type="number"]');
      if (!input) return;
      let val = parseInt(input.value) || 1;
      if (btn.classList.contains('qty-plus'))  val = Math.min(val + 1, 99);
      if (btn.classList.contains('qty-minus')) val = Math.max(val - 1, 1);
      input.value = val;
      input.dispatchEvent(new Event('change'));
    });
  });
}

// ===== CART PAGE ACTIONS =====
function initCartActions() {
  // Update quantity on change
  document.querySelectorAll('.cart-qty-input').forEach(input => {
    input.addEventListener('change', debounce(async () => {
      const itemId  = input.dataset.itemId;
      const qty     = parseInt(input.value);
      await updateCartItem(itemId, qty);
    }, 500));
  });

  // Remove item
  document.querySelectorAll('.cart-remove-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const itemId = btn.dataset.itemId;
      btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
      try {
        const res = await fetch('/api/cart/remove', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ itemId })
        });
        const data = await res.json();
        if (data.success) {
          btn.closest('.cart-item-row')?.remove();
          updateCartCount(data.cartCount);
          checkEmptyCart();
        }
      } catch (err) {
        showNotification('Failed to remove item', 'error');
      }
    });
  });
}

async function updateCartItem(itemId, quantity) {
  try {
    const res = await fetch('/api/cart/update', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemId, quantity })
    });
    const data = await res.json();
    if (data.success) {
      updateCartCount(data.cartCount);
      // Update subtotal display
      const subtotalEl = document.getElementById('cartSubtotal');
      if (subtotalEl && data.subtotal) {
        subtotalEl.textContent = '₹' + parseFloat(data.subtotal).toFixed(2);
      }
    }
  } catch (err) {
    console.error('Cart update failed', err);
  }
}

function checkEmptyCart() {
  const rows = document.querySelectorAll('.cart-item-row');
  if (rows.length === 0) {
    const tableContainer = document.querySelector('.cart-table');
    if (tableContainer) {
      tableContainer.innerHTML = `
        <div style="padding:60px;text-align:center;">
          <div style="font-size:56px;margin-bottom:16px;">🛒</div>
          <h3 style="font-family:var(--font-display);color:var(--forest);margin-bottom:8px;">Your cart is empty</h3>
          <p style="color:var(--text-muted);margin-bottom:24px;">Discover our pure, lab-verified spice collection</p>
          <a href="/shop" class="btn-forest">Browse Products</a>
        </div>
      `;
    }
  }
}

// ===== ADDRESS CARDS =====
function initAddressCards() {
  document.querySelectorAll('.address-card').forEach(card => {
    card.addEventListener('click', () => {
      const radio = card.querySelector('input[type="radio"]');
      if (radio) {
        radio.checked = true;
        document.querySelectorAll('.address-card').forEach(c => c.classList.remove('selected'));
        card.classList.add('selected');
      }
    });
  });

  // Add new address modal toggle
  const addBtn = document.querySelector('.add-address-btn');
  const modal  = document.getElementById('addAddressModal');
  if (addBtn && modal) {
    addBtn.addEventListener('click', () => modal.classList.add('open'));
    modal.querySelector('.modal-close')?.addEventListener('click', () => modal.classList.remove('open'));
  }
}

// ===== PAYMENT OPTIONS =====
function initPaymentOptions() {
  document.querySelectorAll('.payment-option').forEach(opt => {
    opt.addEventListener('click', () => {
      const radio = opt.querySelector('input[type="radio"]');
      if (radio) {
        radio.checked = true;
        document.querySelectorAll('.payment-option').forEach(o => o.classList.remove('selected'));
        opt.classList.add('selected');
      }
    });
  });
}

// ===== COUPON =====
function initCoupon() {
  const form = document.querySelector('.coupon-form');
  if (!form) return;
  form.querySelector('button')?.addEventListener('click', async () => {
    const code = form.querySelector('input')?.value.trim();
    if (!code) return;
    // Just set coupon field in the checkout form
    const hiddenCoupon = document.getElementById('couponCodeHidden');
    if (hiddenCoupon) {
      hiddenCoupon.value = code;
      showNotification('Coupon applied! Discount calculated at checkout.', 'success');
    }
  });
}

// ===== PRODUCT GALLERY =====
function initGallery() {
  const mainImg    = document.getElementById('mainProductImg');
  const thumbs     = document.querySelectorAll('.thumbnail');
  if (!mainImg || !thumbs.length) return;

  thumbs.forEach(thumb => {
    thumb.addEventListener('click', () => {
      thumbs.forEach(t => t.classList.remove('active'));
      thumb.classList.add('active');
      const src = thumb.querySelector('img')?.src;
      if (src) {
        mainImg.style.opacity = '0';
        setTimeout(() => {
          mainImg.src = src;
          mainImg.style.opacity = '1';
        }, 200);
        mainImg.style.transition = 'opacity 0.2s';
      }
    });
  });
}

// ===== SCROLL ANIMATIONS =====
function initScrollAnimations() {
  if (!('IntersectionObserver' in window)) return;
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1 });

  document.querySelectorAll('.product-card, .trust-item, .process-step, .category-card').forEach(el => {
    el.classList.add('fade-up');
    observer.observe(el);
  });
}

// ===== RAZORPAY PAYMENT =====
window.initRazorpay = function(data) {
  const options = {
    key:          data.keyId,
    amount:       data.amount,
    currency:     data.currency || 'INR',
    name:         'Sarvasva Naturals',
    description:  'Order #' + data.orderNumber,
    order_id:     data.razorpayOrderId,
    image:        '/images/logo.png',
    prefill: {
      name:  data.customerName,
      email: data.customerEmail,
      contact: data.customerPhone
    },
    theme: { color: '#1a2e0f' },
    handler: async function(response) {
      try {
        const res = await fetch('/checkout/razorpay/verify', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            razorpay_order_id:   response.razorpay_order_id,
            razorpay_payment_id: response.razorpay_payment_id,
            razorpay_signature:  response.razorpay_signature,
            order_number:        data.orderNumber
          })
        });
        const result = await res.json();
        if (result.success) {
          window.location.href = result.redirect;
        } else {
          window.location.href = '/checkout/failed/' + data.orderNumber;
        }
      } catch (err) {
        window.location.href = '/checkout/failed/' + data.orderNumber;
      }
    },
    modal: {
      ondismiss: function() {
        showNotification('Payment cancelled. Your order is saved — try again anytime.', 'error');
      }
    }
  };
  const rzp = new Razorpay(options);
  rzp.open();
};

// ===== STRIPE PAYMENT =====
window.initStripe = async function(data) {
  const stripe = Stripe(data.publishableKey);
  const elements = stripe.elements();
  const card = elements.create('card', {
    style: {
      base: {
        fontFamily: "'DM Sans', sans-serif",
        fontSize: '16px',
        color: '#1a1a1a',
        '::placeholder': { color: '#aaa' }
      }
    }
  });
  card.mount('#stripe-card-element');

  const form = document.getElementById('stripeForm');
  form?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = form.querySelector('[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Processing...';

    const { error, paymentIntent } = await stripe.confirmCardPayment(data.clientSecret, {
      payment_method: { card }
    });

    if (error) {
      document.getElementById('stripe-error').textContent = error.message;
      btn.disabled = false;
      btn.textContent = 'Pay Now';
    } else if (paymentIntent.status === 'succeeded') {
      const res = await fetch('/checkout/stripe/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ paymentIntentId: paymentIntent.id, orderNumber: data.orderNumber })
      });
      const result = await res.json();
      window.location.href = result.redirect;
    }
  });
};

// ===== NOTIFICATION =====
function showNotification(message, type = 'success') {
  const el = document.createElement('div');
  el.className = `flash-message flash-${type}`;
  el.innerHTML = `
    <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
    <span>${message}</span>
    <button class="flash-close"><i class="fas fa-times"></i></button>
  `;
  document.body.appendChild(el);
  el.querySelector('.flash-close').addEventListener('click', () => el.remove());
  setTimeout(() => { el.style.opacity = '0'; setTimeout(() => el.remove(), 300); }, 4000);
}

// ===== ADMIN HELPERS =====
window.toggleProduct = async function(id, btn) {
  const res = await fetch(`/admin/products/toggle/${id}`, { method: 'POST' });
  const data = await res.json();
  btn.textContent = data.active ? 'Active' : 'Inactive';
  btn.className = `btn-sm ${data.active ? 'edit' : 'delete'}`;
};

window.confirmDelete = function(msg) {
  return confirm(msg || 'Are you sure you want to delete this item?');
};

// ===== UTILS =====
function debounce(fn, ms) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

// ===== CSS ANIMATION for bounce =====
const style = document.createElement('style');
style.textContent = `
  .bounce { animation: cartBounce 0.4s ease; }
  @keyframes cartBounce {
    0%,100% { transform: scale(1); }
    50%      { transform: scale(1.4); }
  }
  .fade-up { opacity: 0; transform: translateY(24px); transition: opacity 0.5s ease, transform 0.5s ease; }
  .fade-up.visible { opacity: 1; transform: translateY(0); }
`;
document.head.appendChild(style);
