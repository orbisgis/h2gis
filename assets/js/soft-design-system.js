/*!

=========================================================
* Soft UI Design System - v1.0.9
=========================================================

* Product Page: https://www.creative-tim.com/product/soft-ui-design-system
* Copyright 2021 Creative Tim (https://www.creative-tim.com)

* Coded by Creative Tim

=========================================================

* The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

*/

// initialization of Popovers
var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'))
var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
  return new bootstrap.Popover(popoverTriggerEl)
})

// initialization of Tooltips
var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
  return new bootstrap.Tooltip(tooltipTriggerEl)
})

// helper for adding on all elements multiple attributes
function setAttributes(el, options) {
  Object.keys(options).forEach(function(attr) {
    el.setAttribute(attr, options[attr]);
  })
}

// activate popovers
var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="popover"]'))
var popoverList = popoverTriggerList.map(function(popoverTriggerEl) {
  return new bootstrap.Popover(popoverTriggerEl)
})

// activate tooltips
var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-toggle="tooltip"]'))
var tooltipList = tooltipTriggerList.map(function(tooltipTriggerEl) {
  return new bootstrap.Tooltip(tooltipTriggerEl)
})

// Tabs navigation

var total = document.querySelectorAll('.nav-pills');

total.forEach(function(item, i) {
  var moving_div = document.createElement('div');
  var first_li = item.querySelector('li:first-child .nav-link');
  var tab = first_li.cloneNode();
  tab.innerHTML = "-";

  moving_div.classList.add('moving-tab', 'position-absolute', 'nav-link');
  moving_div.appendChild(tab);
  item.appendChild(moving_div);

  var list_length = item.getElementsByTagName("li").length;

  moving_div.style.padding = '0px';
  moving_div.style.width = item.querySelector('li:nth-child(1)').offsetWidth + 'px';
  moving_div.style.transform = 'translate3d(0px, 0px, 0px)';
  moving_div.style.transition = '.5s ease';

  item.onmouseover = function(event) {
    let target = getEventTarget(event);
    let li = target.closest('li'); // get reference
    if (li) {
      let nodes = Array.from(li.closest('ul').children); // get array
      let index = nodes.indexOf(li) + 1;
      item.querySelector('li:nth-child(' + index + ') .nav-link').onclick = function() {
        moving_div = item.querySelector('.moving-tab');
        let sum = 0;
        if (item.classList.contains('flex-column')) {
          for (var j = 1; j <= nodes.indexOf(li); j++) {
            sum += item.querySelector('li:nth-child(' + j + ')').offsetHeight;
          }
          moving_div.style.transform = 'translate3d(0px,' + sum + 'px, 0px)';
          moving_div.style.height = item.querySelector('li:nth-child(' + j + ')').offsetHeight;
        } else {
          for (var j = 1; j <= nodes.indexOf(li); j++) {
            sum += item.querySelector('li:nth-child(' + j + ')').offsetWidth;
          }
          moving_div.style.transform = 'translate3d(' + sum + 'px, 0px, 0px)';
          moving_div.style.width = item.querySelector('li:nth-child(' + index + ')').offsetWidth + 'px';
        }
      }
    }
  }
});


// Tabs navigation resize

window.addEventListener('resize', function(event) {
  total.forEach(function(item, i) {
    item.querySelector('.moving-tab').remove();
    var moving_div = document.createElement('div');
    var tab = item.querySelector(".nav-link.active").cloneNode();
    tab.innerHTML = "-";

    moving_div.classList.add('moving-tab', 'position-absolute', 'nav-link');
    moving_div.appendChild(tab);

    item.appendChild(moving_div);

    moving_div.style.padding = '0px';
    moving_div.style.transition = '.5s ease';

    let li = item.querySelector(".nav-link.active").parentElement;

    if (li) {
      let nodes = Array.from(li.closest('ul').children); // get array
      let index = nodes.indexOf(li) + 1;

      let sum = 0;
      if (item.classList.contains('flex-column')) {
        for (var j = 1; j <= nodes.indexOf(li); j++) {
          sum += item.querySelector('li:nth-child(' + j + ')').offsetHeight;
        }
        moving_div.style.transform = 'translate3d(0px,' + sum + 'px, 0px)';
        moving_div.style.width = item.querySelector('li:nth-child(' + index + ')').offsetWidth + 'px';
        moving_div.style.height = item.querySelector('li:nth-child(' + j + ')').offsetHeight;
      } else {
        for (var j = 1; j <= nodes.indexOf(li); j++) {
          sum += item.querySelector('li:nth-child(' + j + ')').offsetWidth;
        }
        moving_div.style.transform = 'translate3d(' + sum + 'px, 0px, 0px)';
        moving_div.style.width = item.querySelector('li:nth-child(' + index + ')').offsetWidth + 'px';

      }
    }
  });

  if (window.innerWidth < 991) {
    total.forEach(function(item, i) {
      if (!item.classList.contains('flex-column')) {
        item.classList.add('flex-column', 'on-resize');
      }
    });
  } else {
    total.forEach(function(item, i) {
      if (item.classList.contains('on-resize')) {
        item.classList.remove('flex-column', 'on-resize');
      }
    })
  }
});


function getEventTarget(e) {
  e = e || window.event;
  return e.target || e.srcElement;
}

// End tabs navigation

// Copy code function

function copyCode(el) {
  const selection = window.getSelection();
  const range = document.createRange();
  const textToCopy = el.nextElementSibling;
  range.selectNodeContents(textToCopy);
  selection.removeAllRanges();
  selection.addRange(range);
  const successful = document.execCommand('copy');
  window.getSelection().removeAllRanges()
  if (!el.parentElement.querySelector('.alert')) {
    var alert = document.createElement('div');
    alert.classList.add('alert', 'alert-success', 'position-absolute', 'top-0', 'border-0', 'text-white', 'w-25', 'end-0', 'start-0', 'mt-2', 'mx-auto', 'py-2');
    alert.style.transform = 'translate3d(0px, 0px, 0px)'
    alert.style.opacity = '0';
    alert.style.transition = '.35s ease';
    setTimeout(function() {
      alert.style.transform = 'translate3d(0px, 20px, 0px)';
      alert.style.setProperty("opacity", "1", "important");
    }, 100);
    alert.innerHTML = "Code successfully copied!";
    el.parentElement.appendChild(alert);
    setTimeout(function() {
      alert.style.transform = 'translate3d(0px, 0px, 0px)'
      alert.style.setProperty("opacity", "0", "important");
    }, 2000);
    setTimeout(function() {
      el.parentElement.querySelector('.alert').remove();
    }, 2500);
  }
}

// End copy code function

// Input focus function
var getParent = function(elem, selector) {
  for (; elem && elem !== document; elem = elem.parentNode) {
    if (elem.matches(selector)) return elem;
  }
  return null;
};
document.addEventListener('click', function(event) {
  var parent = getParent(event.target, '.input-group');
  if (event.target.classList.contains('form-control')) {
    var focus = document.querySelectorAll('.input-group.focused');
    for (var i = 0; i < focus.length; i++) {
      focus[i].classList.remove('focused');
    }
    parent.classList.add('focused');
  }
  var focus = document.querySelectorAll('.input-group.focused');
  if (focus && event.target != parent && event.target.parentNode != parent) {
    for (var i = 0; i < focus.length; i++) {
      focus[i].classList.remove('focused');
    }

  }
}, false);


// Returns a function, that, as long as it continues to be invoked, will not
// be triggered. The function will be called after it stops being called for
// N milliseconds. If `immediate` is passed, trigger the function on the
// leading edge, instead of the trailing.

function debounce(func, wait, immediate) {
  var timeout;
  return function() {
    var context = this,
      args = arguments;
    clearTimeout(timeout);
    timeout = setTimeout(function() {
      timeout = null;
      if (!immediate) func.apply(context, args);
    }, wait);
    if (immediate && !timeout) func.apply(context, args);
  };
};