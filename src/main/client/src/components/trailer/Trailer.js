import { useParams, useNavigate } from 'react-router-dom';
import ReactPlayer from 'react-player/youtube';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faArrowLeft } from '@fortawesome/free-solid-svg-icons';
import { useTranslation } from 'react-i18next';
import './Trailer.css';
import React, { useEffect, useState } from 'react';

const Trailer = () => {
    const { t } = useTranslation();
    const params = useParams();
    const navigate = useNavigate();
    const key = params.ytTrailerId;
    const [movieTitle, setMovieTitle] = useState(t('hero.movieTrailer'));

    // Go back to previous page
    const goBack = () => {
        navigate(-1);
    };

    // Handle escape key
    useEffect(() => {
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                goBack();
            }
        };

        document.addEventListener('keydown', handleEscape);
        return () => document.removeEventListener('keydown', handleEscape);
    }, []);

    return (
        <div className="trailer-fullscreen-container">
            <div className="trailer-controls">
                <button className="trailer-back-btn" onClick={goBack}>
                    <FontAwesomeIcon icon={faArrowLeft} />
{t('common.back')}
                </button>
            </div>
            
            <div className="react-player-container">
                {key && (
                    <ReactPlayer
                        url={`https://www.youtube.com/watch?v=${key}`}
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
                )}
            </div>
        </div>
    );
};

export default Trailer