import React, { useEffect, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCheck, faTimes, faHeart } from '@fortawesome/free-solid-svg-icons';
import './NotificationPopup.css';

const NotificationPopup = ({ 
  isVisible, 
  message, 
  type = 'success', // success, error, info
  action = 'added', // added, removed, error
  onClose,
  duration = 3000
}) => {
  const [shouldShow, setShouldShow] = useState(false);

  useEffect(() => {
    if (isVisible) {
      setShouldShow(true);
      const timer = setTimeout(() => {
        setShouldShow(false);
        setTimeout(() => {
          onClose && onClose();
        }, 300); // CSS transition duration
      }, duration);
      
      return () => clearTimeout(timer);
    }
  }, [isVisible, duration, onClose]);

  const getIcon = () => {
    switch (action) {
      case 'added':
        return faHeart;
      case 'removed':
        return faHeart;
      case 'error':
        return faTimes;
      default:
        return faCheck;
    }
  };

  const getIconColor = () => {
    switch (type) {
      case 'error':
        return '#dc3545';
      case 'info':
        return '#17a2b8';
      case 'success':
      default:
        return action === 'removed' ? '#ffc107' : '#28a745';
    }
  };

  if (!isVisible && !shouldShow) return null;

  return (
    <div className={`notification-popup ${shouldShow ? 'show' : 'hide'}`}>
      <div className="notification-content">
        <div className="notification-icon" style={{ color: getIconColor() }}>
          <FontAwesomeIcon icon={getIcon()} />
        </div>
        <div className="notification-text">
          <div className="notification-message">{message}</div>
        </div>
        <button 
          className="notification-close" 
          onClick={() => {
            setShouldShow(false);
            setTimeout(() => onClose && onClose(), 300);
          }}
        >
          <FontAwesomeIcon icon={faTimes} />
        </button>
      </div>
      <div className="notification-progress">
        <div className="notification-progress-bar"></div>
      </div>
    </div>
  );
};

export default NotificationPopup;
