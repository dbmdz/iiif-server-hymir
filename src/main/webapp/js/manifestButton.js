var ManifestButton = {
  /* all of the needed locales */
  locales: {
    'de': {
      'button-tooltip': 'Manifest dieses Objekts anzeigen',
    },
    'en': {
      'button-tooltip': 'View manifest of this object'
    }
  },

  /* the template for the link button */
  buttonTemplate: Mirador.Handlebars.compile([
    '<a target="_blank" class="mirador-btn mirador-icon-manifest-link" role="button" title="{{t "button-tooltip"}}" aria-label="{{t "button-tooltip"}}">',
    '<i class="fa {{iconClass}} fa-lg fa-fw"></i>',
    '</a>',
  ].join('')),

  /* initializes the plugin */
  init: function(){
    i18next.on('initialized', function(){
      this.addLocalesToViewer();
    }.bind(this));
    this.injectWorkspaceEventHandler();
  },

  /* injects the button to the window menu */
  injectButtonToMenu: function(windowButtons, iconClass){
    $(windowButtons).prepend(this.buttonTemplate({
      'iconClass': iconClass || 'fa-file-text-o'
    }));
  },

  /* injects the needed workspace event handler */
  injectWorkspaceEventHandler: function(){
    var this_ = this;
    var origFunc = Mirador.Workspace.prototype.bindEvents;
    Mirador.Workspace.prototype.bindEvents = function(){
      origFunc.apply(this);
      this.eventEmitter.subscribe('WINDOW_ELEMENT_UPDATED', function(event, data){
        var windowButtons = data.element.find('.window-manifest-navigation');
        var options = this.state.getStateProperty('manifestButton');
        var iconClass = $.isPlainObject(options) ? options.iconClass : undefined;
        this_.injectButtonToMenu(windowButtons, iconClass);
      }.bind(this));
      this.eventEmitter.subscribe('windowUpdated', function(event, data){
        if(!data.loadedManifest){
          return;
        }
        var slotElement = this.getSlotFromAddress(data.slotAddress).appendTo;
        $('.mirador-btn.mirador-icon-manifest-link', slotElement).attr('href', data.loadedManifest);
      }.bind(this));
    };
  },

  /* adds the locales to the internationalization module of the viewer */
  addLocalesToViewer: function(){
    for(var language in this.locales){
      i18next.addResources(
        language, 'translation',
        this.locales[language]
      );
    }
  }
}

$(document).ready(function(){
  ManifestButton.init();
});
