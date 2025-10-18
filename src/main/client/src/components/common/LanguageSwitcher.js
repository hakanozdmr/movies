import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faGlobe, faFlag } from '@fortawesome/free-solid-svg-icons';

const LanguageSwitcher = () => {
  const { i18n, t } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);

  const changeLanguage = (lng) => {
    i18n.changeLanguage(lng);
    setIsOpen(false);
  };

  const currentLanguage = i18n.language;
  const isTurkish = currentLanguage === 'tr';

  return (
    <div className="language-switcher-container">
      <button 
        className="language-toggle-btn"
        onClick={() => setIsOpen(!isOpen)}
      >
        <div className="language-btn-content">
          <div className="language-icon">
            <FontAwesomeIcon icon={faFlag} />
          </div>
          <div className="language-text">
            {isTurkish ? 'TR' : 'EN'}
          </div>
          <div className={`toggle-arrow ${isOpen ? 'open' : ''}`}>
            ▼
          </div>
        </div>
      </button>

      {isOpen && (
        <div className="language-dropdown-menu">
          <div className="language-options">
            <button
              className={`language-option ${isTurkish ? 'active' : ''}`}
              onClick={() => changeLanguage('tr')}
            >
              <span className="flag">🇹🇷</span>
              <span className="language-name">{t('language.turkish')}</span>
              {isTurkish && <div className="check-mark">✓</div>}
            </button>
            
            <button
              className={`language-option ${!isTurkish ? 'active' : ''}`}
              onClick={() => changeLanguage('en')}
            >
              <span className="flag">🇺🇸</span>
              <span className="language-name">{t('language.english')}</span>
              {!isTurkish && <div className="check-mark">✓</div>}
            </button>
          </div>
        </div>
      )}
      
      {/* Backdrop için overlay */}
      {isOpen && (
        <div 
          className="language-backdrop" 
          onClick={() => setIsOpen(false)}
        />
      )}
    </div>
  );
};

export default LanguageSwitcher;
