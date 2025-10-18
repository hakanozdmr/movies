import React, { useEffect } from 'react';
import ReactPlayer from 'react-player/youtube';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faTimes } from '@fortawesome/free-solid-svg-icons';
import './TrailerModal.css';

const TrailerModal = ({ isOpen, onClose, trailerId, title = "Film Fragmanı" }) => {
  
  useEffect(() => {
    // Close modal on Escape key press
    const handleEscape = (e) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      // Prevent body scroll when modal is open
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, onClose]);

  if (!isOpen || !trailerId) return null;

  return (
    <div className="trailer-modal-overlay" onClick={onClose}>
      <div className="trailer-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="trailer-modal-header">
          <h3 className="trailer-modal-title">{title}</h3>
          <button 
            className="trailer-modal-close-btn"
            onClick={onClose}
            aria-label="Close trailer"
          >
            <FontAwesomeIcon icon={faTimes} />
          </button>
        </div>
        
        <div className="trailer-modal-player-container">
          <ReactPlayer
            url={`https://www.youtube.com/watch?v=${trailerId}`}
            width="100%"
            height="100%"
            playing={true}
            controls={true}
            config={{
              youtube: {
                playerVars: {
                  autoplay: 1,
                  modestbranding: 1,
                  rel: 0,
                  showinfo: 0,
                  iv_load_policy: 3,
                }
              }
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default TrailerModal;

