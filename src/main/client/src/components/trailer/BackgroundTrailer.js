import React, { forwardRef } from 'react';
import ReactPlayer from 'react-player/youtube';
import './BackgroundTrailer.css';

const BackgroundTrailer = forwardRef(({ 
  trailerId, 
  isPlaying: externalIsPlaying = false, 
  isMuted: externalIsMuted = true,
  onVideoClick 
}, ref) => {
  // External props'ları doğrudan kullan, internal state kullanma

  if (!trailerId) return null;

  return (
    <div 
      className="background-trailer-container" 
      ref={ref}
      onClick={(e) => {
        console.log('BackgroundTrailer main container clicked');
        e.preventDefault();
        e.stopPropagation();
        // YouTube toggle'ını engelle ama kendi handler'ımızı çağır
        if (onVideoClick) {
          console.log('Calling onVideoClick from main container');
          onVideoClick();
        } else {
          console.log('onVideoClick handler not available in main container');
        }
        return false;
      }}
    >
      <div 
        className="background-trailer-player"
        onClick={(e) => {
          console.log('BackgroundTrailer container clicked');
          e.preventDefault();
          e.stopPropagation();
          // YouTube toggle'ını engelle ama kendi handler'ımızı çağır
          if (onVideoClick) {
            console.log('Calling onVideoClick handler');
            onVideoClick();
          } else {
            console.log('onVideoClick handler not available');
          }
          return false;
        }}
      >
        <ReactPlayer
          url={`https://www.youtube-nocookie.com/watch?v=${trailerId}`}
          width="120%"
          height="120%"
          playing={externalIsPlaying}
          muted={externalIsMuted}
          loop={true}
          pip={false}
          stopOnUnmount={false}
          config={{
            youtube: {
              playerVars: {
                // Playback settings
                autoplay: 1,
                mute: 1,
                loop: 1,
                
                // Video quality settings
                vq: 'hd1080',
                
                // Hide all YouTube UI elements
                controls: 0,
                showinfo: 0,
                modestbranding: 1,
                rel: 0,
                iv_load_policy: 3,
                cc_load_policy: 0,
                fs: 0,
                disablekb: 1,
                playsinline: 1,
                start: 0,
                
                // Önerileri gizle
                showrelated: 0,
                playlist: trailerId,
                
                // Additional hiding parameters
                showtitle: 0,
                enablejsapi: 0, // JavaScript API'yi devre dışı bırak - tıklama engelleme için
                
                // Tıklama ve kontrol engelleme
                widget_referrer: 'https://www.youtube.com/',
                origin: window.location.origin,
                
                // YouTube player'ın tıklama olaylarını engelle
                html5: 1,
                wmode: 'opaque',
                playerapiid: 'none',
                end: 0,
                autohide: 1
              },
              embedOptions: {
                origin: window.location.origin
              }
            }
          }}
        />
      </div>
      
      {/* Gradient overlay for better text visibility */}
      <div className="background-trailer-overlay"></div>
    </div>
  );
});

export default BackgroundTrailer;
