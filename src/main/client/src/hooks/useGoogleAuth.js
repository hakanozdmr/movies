import { useEffect, useState, useCallback, useRef } from 'react';

const useGoogleAuth = (clientId) => {
  const [isLoaded, setIsLoaded] = useState(false);
  const [gsi, setGsi] = useState(null);
  const initializedRef = useRef(false);

  useEffect(() => {
    const checkGoogleLoaded = () => {
      if (window.google && window.google.accounts) {
        setIsLoaded(true);
        setGsi(window.google.accounts);
        return true;
      }
      return false;
    };

    if (checkGoogleLoaded()) {
      return;
    }

    const timer = setInterval(() => {
      if (checkGoogleLoaded()) {
        clearInterval(timer);
      }
    }, 100);

    return () => clearInterval(timer);
  }, []);

  const initializeGoogleSignIn = useCallback((callback) => {
    if (!isLoaded || !gsi || !clientId || initializedRef.current) {
      return;
    }

    try {
      gsi.id.initialize({
        client_id: clientId,
        callback: callback,
        auto_select: false,
        cancel_on_tap_outside: true,
      });
      initializedRef.current = true;
    } catch (error) {
      console.error('Google Sign-In initialization failed:', error);
    }
  }, [isLoaded, gsi, clientId]);

  const renderGoogleSignInButton = useCallback((elementId) => {
    if (!isLoaded || !gsi || !elementId) {
      return;
    }

    const element = document.getElementById(elementId);
    if (!element) {
      console.error(`Element with id ${elementId} not found`);
      return;
    }

    // Clear any existing content
    element.innerHTML = '';

    try {
      gsi.id.renderButton(element, {
        theme: 'outline',
        size: 'large',
        type: 'standard',
        text: 'signin_with',
        shape: 'rectangular',
        logo_alignment: 'left',
      });
    } catch (error) {
      console.error('Google Sign-In button rendering failed:', error);
    }
  }, [isLoaded, gsi]);

  return {
    isLoaded,
    initializeGoogleSignIn,
    renderGoogleSignInButton,
  };
};

export default useGoogleAuth;
