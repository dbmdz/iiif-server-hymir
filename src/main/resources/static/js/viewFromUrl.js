/* Polyfill for IE not supporting URLSearchParams */
if(typeof window.URLSearchParams === 'undefined'){
  /*! (C) Andrea Giammarchi - Mit Style License */
  window.URLSearchParams=function(){"use strict";function e(e){var n,r,i,s,o,l,c=Object.create(null);this[u]=c;if(!e)return;if(typeof e=="string"){e.charAt(0)==="?"&&(e=e.slice(1));for(s=e.split("&"),o=0,l=s.length;o<l;o++)i=s[o],n=i.indexOf("="),-1<n?a(c,f(i.slice(0,n)),f(i.slice(n+1))):i.length&&a(c,f(i),"")}else if(t(e))for(o=0,l=e.length;o<l;o++)i=e[o],a(c,i[0],i[1]);else for(r in e)a(c,r,e[r])}function a(e,n,r){n in e?e[n].push(""+r):e[n]=t(r)?r:[""+r]}function f(e){return decodeURIComponent(e.replace(i," "))}function l(e){return encodeURIComponent(e).replace(r,o)}var t=Array.isArray,n=e.prototype,r=/[!'\(\)~]|%20|%00/g,i=/\+/g,s={"!":"%21","'":"%27","(":"%28",")":"%29","~":"%7E","%20":"+","%00":"\0"},o=function(e){return s[e]},u="__URLSearchParams__:"+Math.random();n.append=function(t,n){a(this[u],t,n)},n.delete=function(t){delete this[u][t]},n.get=function(t){var n=this[u];return t in n?n[t][0]:null},n.getAll=function(t){var n=this[u];return t in n?n[t].slice(0):[]},n.has=function(t){return t in this[u]},n.set=function(t,n){this[u][t]=[""+n]},n.forEach=function(t,n){var r=this[u];Object.getOwnPropertyNames(r).forEach(function(e){r[e].forEach(function(r){t.call(n,r,e,this)},this)},this)},n.toJSON=function(){return{}},n.toString=function y(){var e=this[u],t=[],n,r,i,s;for(r in e){i=l(r);for(n=0,s=e[r];n<s.length;n++)t.push(i+"="+l(s[n]))}return t.join("&")};var c=Object.defineProperty,h=Object.getOwnPropertyDescriptor,p=function(e){function t(t,r){n.append.call(this,t,r),t=this.toString(),e.set.call(this._usp,t?"?"+t:"")}function r(t){n.delete.call(this,t),t=this.toString(),e.set.call(this._usp,t?"?"+t:"")}function i(t,r){n.set.call(this,t,r),t=this.toString(),e.set.call(this._usp,t?"?"+t:"")}return function(e,n){return e.append=t,e.delete=r,e.set=i,c(e,"_usp",{configurable:!0,writable:!0,value:n})}},d=function(e){return function(t,n){return c(t,"_searchParams",{configurable:!0,writable:!0,value:e(n,t)}),n}},v=function(t){var r=t.append;t.append=n.append,e.call(t,t._usp.search.slice(1)),t.append=r},m=function(e,t){if(!(e instanceof t))throw new TypeError("'searchParams' accessed on an object that does not implement interface "+t.name)},g=function(t){var n=t.prototype,r=h(n,"searchParams"),i=h(n,"href"),s=h(n,"search"),o;!r&&s&&s.set&&(o=d(p(s)),Object.defineProperties(n,{href:{get:function(){return i.get.call(this)},set:function(e){var t=this._searchParams;i.set.call(this,e),t&&v(t)}},search:{get:function(){return s.get.call(this)},set:function(e){var t=this._searchParams;s.set.call(this,e),t&&v(t)}},searchParams:{get:function(){return m(this,t),this._searchParams||o(this,new e(this.search.slice(1)))},set:function(e){m(this,t),o(this,e)}}}))};return g(HTMLAnchorElement),/^function|object$/.test(typeof URL)&&URL.prototype&&g(URL),e}();(function(e){var t=function(){try{return!!Symbol.iterator}catch(e){return!1}}();"forEach"in e||(e.forEach=function(t,n){var r=Object.create(null);this.toString().replace(/=[\s\S]*?(?:&|$)/g,"=").split("=").forEach(function(e){if(!e.length||e in r)return;(r[e]=this.getAll(e)).forEach(function(r){t.call(n,r,e,this)},this)},this)}),"keys"in e||(e.keys=function(){var n=[];this.forEach(function(e,t){n.push(t)});var r={next:function(){var e=n.shift();return{done:e===undefined,value:e}}};return t&&(r[Symbol.iterator]=function(){return r}),r}),"values"in e||(e.values=function(){var n=[];this.forEach(function(e){n.push(e)});var r={next:function(){var e=n.shift();return{done:e===undefined,value:e}}};return t&&(r[Symbol.iterator]=function(){return r}),r}),"entries"in e||(e.entries=function(){var n=[];this.forEach(function(e,t){n.push([t,e])});var r={next:function(){var e=n.shift();return{done:e===undefined,value:e}}};return t&&(r[Symbol.iterator]=function(){return r}),r}),t&&!(Symbol.iterator in e)&&(e[Symbol.iterator]=e.entries),"sort"in e||(e.sort=function(){var t=this.entries(),n=t.next(),r=n.done,i=[],s=Object.create(null),o,u,a;while(!r)a=n.value,u=a[0],i.push(u),u in s||(s[u]=[]),s[u].push(a[1]),n=t.next(),r=n.done;i.sort();for(o=0;o<i.length;o++)this.delete(i[o]);for(o=0;o<i.length;o++)u=i[o],this.append(u,s[u].shift())})})(URLSearchParams.prototype);
}

var UpdateUrlFromView = {
  updateUrl: false,
  // represents query params not added by this plugin
  furtherParams: '',

  init: function() {
    var this_ = this;
    var origFunc = Mirador.Viewer.prototype.setupViewer;
    Mirador.Viewer.prototype.setupViewer = function() {
      if (window.location.search) {
        var params = this_.parseRequestParams(window.location.search);
        this_.furtherParams = Object.keys(params).reduce(function(paramString, paramKey){
          if(!/(view|manifest|canvas)/.test(paramKey)){
            return paramString + paramKey + '=' + params[paramKey] + '&';
          }
          return paramString;
        }, '');
        if (params.hasOwnProperty('manifest')) {
          this.data.unshift({'manifestUri': params.manifest});
          var windowObj = {
            viewType: params.view || 'ImageView',
            loadedManifest: params.manifest,
            canvasID: params.canvas
          };
          this.state.currentConfig.windowObjects = [
            $.extend(true, {}, this.state.currentConfig.windowObjects[0], windowObj)
          ];
        }
      }
      this.eventEmitter.subscribe('slotsUpdated', this_.onSlotsUpdated.bind(this_));
      this.eventEmitter.subscribe('windowUpdated', this_.onWindowUpdated.bind(this_));
      origFunc.apply(this);
    }
  },

  parseRequestParams: function(qstr) {
    var query = {};
    var a = (qstr[0] === '?' ? qstr.substr(1) : qstr).split('&');
    for (var i = 0; i < a.length; i++) {
      var b = a[i].split('=');
      query[decodeURIComponent(b[0])] = decodeURIComponent(b[1] || '');
    }
    return query;
  },

  onSlotsUpdated: function(_, data) {
    this.updateUrl = data.slots.length === 1;
    if (!this.updateUrl) {
      window.history.replaceState({}, null, "?");
    } else if (data.slots[0].window !== null) {
      window.history.replaceState({}, null, "?" + this.constructSearchParams(data.slots[0].window));
    }
  },

  onWindowUpdated: function(_, data) {
    if (!this.updateUrl || !data.viewType) {
      return;
    }
    window.history.replaceState({}, null, "?" + this.constructSearchParams(data));
  },

  constructSearchParams: function(data) {
    var newParams = new URLSearchParams(this.furtherParams);
    newParams.set('view', data.viewType);
    if (data.loadedManifest) {
      newParams.set('manifest', data.loadedManifest);
    } else {
      newParams.set('manifest', data.manifest.uri);
    }
    if (data.canvasID) {
      newParams.set('canvas', data.canvasID);
    }
    return newParams.toString();
  }
}

$(document).ready(function() {
  UpdateUrlFromView.init();
});
