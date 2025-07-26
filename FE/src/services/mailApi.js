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
