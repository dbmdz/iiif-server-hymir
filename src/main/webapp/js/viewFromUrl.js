var UpdateUrlFromView = {
  updateUrl: false,

  init: function() {
    var this_ = this;
    var origFunc = Mirador.Viewer.prototype.setupViewer;
    Mirador.Viewer.prototype.setupViewer = function() {
      if (window.location.search) {
        var params = this_.parseRequestParams(window.location.search);
        this.data.unshift({'manifestUri': params.manifest});
        var windowObj = {
          viewType: params.view || 'ImageView',
          loadedManifest: params.manifest,
          canvasID: params.canvas
        };
        this.state['currentConfig'].windowObjects = [windowObj];
      }
      this.eventEmitter.subscribe('slotsUpdated', this_.onSlotsUpdated);
      this.eventEmitter.subscribe('windowUpdated', this_.onWindowUpdated);
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
    } else {
      console.log('Activated URL update');
    }
  },

  onWindowUpdated: function(_, data) {
    if (!this.updateUrl || !data.viewType) {
      return;
    }
    var newParams = new URLSearchParams();
    newParams.set('view', data.viewType);
    newParams.set('manifest', data.loadedManifest);
    if (data.canvasID) {
      newParams.set('canvas', data.canvasID);
    }
    window.history.replaceState({}, null, "?" + newParams.toString());
  }
}

$(document).ready(function() {
  UpdateUrlFromView.init();
});
