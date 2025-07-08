import axios from 'axios';

const API_URL = 'http://localhost:8080/mailapp'; 

const authHeader = () => ({
    headers: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
    }
});

export const getInbox = async () => {
    const response = await axios.get(`${API_URL}/mail/inbox`, authHeader());
    return response.data;
};

export const getSent = async () => {
    const response = await axios.get(`${API_URL}/mail/sent`, authHeader());
    return response.data;
};

export const getEmailById = async (id) => {
    const response = await axios.get(`${API_URL}/mail/email/${id}`, authHeader());
    return response.data;
};

export const sendMail = async (mail) => {
    const response = await axios.post(`${API_URL}/mail/sendmail`, mail, authHeader());
    return response.data;
};