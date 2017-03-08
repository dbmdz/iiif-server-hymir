var UpdateUrlFromView = {
  updateUrl: false,

  init: function() {
    var this_ = this;
    var origFunc = Mirador.Viewer.prototype.setupViewer;
    Mirador.Viewer.prototype.setupViewer = function() {
      if (window.location.hash) {
        var windowObj = this_.parseHash(window.location.hash);
        this.data.unshift({'manifestUri': windowObj.loadedManifest});
        this.state['currentConfig'].windowObjects = [windowObj];
      }
      this.eventEmitter.subscribe('slotsUpdated', this_.onSlotsUpdated);
      this.eventEmitter.subscribe('windowUpdated', this_.onWindowUpdated);
      origFunc.apply(this);
    }
  },

  parseHash: function(hash) {
    var parts = hash.substr(1).split(';');
    var config = {
      viewType: parts[0],
      loadedManifest: parts[1]
    };
    if (parts.length > 2) {
      config.canvasID = parts[2];
    }
    return config;
  },

  onSlotsUpdated: function(_, data) {
    this.updateUrl = data.slots.length === 1;
    if (!this.updateUrl) {
      window.location.hash = '';
    }
  },

  onWindowUpdated: function(_, data) {
    if (!this.updateUrl || !data.viewType) {
      return;
    }
    var hash = '#' + data.viewType + ';' + data.loadedManifest;
    if (data.canvasID) {
      hash += ';' + data.canvasID;
    }
    window.location.hash = hash;
  }
}

$(document).ready(function() {
  UpdateUrlFromView.init();
});
