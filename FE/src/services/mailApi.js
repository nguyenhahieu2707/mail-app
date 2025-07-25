// import axios from 'axios';

// const API_URL = 'http://backend:8080'; 

// const authHeader = () => ({
//     headers: {
//         Authorization: `Bearer ${localStorage.getItem('accessToken')}`
//     }
// });

// export const getInbox = async () => {
//     const response = await axios.get(`${API_URL}/mail/inbox`, authHeader());
//     return response.data;
// };

// export const getSent = async () => {
//     const response = await axios.get(`${API_URL}/mail/sent`, authHeader());
//     return response.data;
// };

// export const getEmailById = async (id) => {
//     const response = await axios.get(`${API_URL}/mail/email/${id}`, authHeader());
//     return response.data;
// };

// export const sendMail = async (formData) => {
//   const response = await axios.post(`${API_URL}/mail/sendmail`, formData, {
//     headers: {
//       Authorization: `Bearer ${localStorage.getItem('accessToken')}`
//     }
//   });
//   return response.data;
// };


// export const searchEmails = async (searchQuery) => {
//   const response = await axios.post(
//     `${API_URL}/mail/search`,
//     { query: searchQuery, page: 0, size: 10 },
//     authHeader()
//   );
//   return response.data;
// };


import axios from 'axios';
import { remoteLogger } from '../utils/remoteLogger';


const authHeader = () => ({
  headers: {
    Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  }
});

export const getInbox = async () => {
  try {
    const response = await axios.get(`/mail/inbox`, authHeader());
    return response.data;
  } catch (error) {
    remoteLogger.error(`getInbox failed: ${error.message}`);
    throw error;
  }
};

export const getSent = async () => {
  try {
    const response = await axios.get(`/mail/sent`, authHeader());
    return response.data;
  } catch (error) {
    remoteLogger.error(`getSent failed: ${error.message}`);
    throw error;
  }
};

export const getEmailById = async (id) => {
  try {
    const response = await axios.get(`/mail/email/${id}`, authHeader());
    return response.data;
  } catch (error) {
    remoteLogger.error(`getEmailById failed (id=${id}): ${error.message}`);
    throw error;
  }
};

export const getInboxEmail = async (uid) => {
  try {
    const response = await axios.get(`/mail/email/inbox/${uid}`, authHeader());
    return response.data;
  } catch (error) {
    remoteLogger.error(`getInboxEmail failed (uid=${uid}): ${error.message}`);
    throw error;
  }
};

export const sendMail = async (formData) => {
  try {
    const response = await axios.post(`/mail/sendmail`, formData, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
      }
    });
    return response.data;
  } catch (error) {
    remoteLogger.error(`sendMail failed: ${error.message}`);
    throw error;
  }
};

export const searchEmails = async (searchQuery) => {
  try {
    const response = await axios.post(
      `/mail/search`,
      { query: searchQuery, page: 0, size: 10 },
      authHeader()
    );
    return response.data;
  } catch (error) {
    remoteLogger.error(`searchEmails failed (query="${searchQuery}"): ${error.message}`);
    throw error;
  }
};
