import axios from 'axios';

const API_URL = 'http://localhost:8080/mailapp'; 

export const getInbox = async () => {
    const response = await axios.get(`${API_URL}/mail/inbox`);
    return response.data;
};

export const getSent = async () => {
    const response = await axios.get(`${API_URL}/mail/sent`);
    return response.data;
};

export const getEmailById = async (id) => {
    const response = await axios.get(`${API_URL}/mail/email/${id}`);
    return response.data;
};

export const sendMail = async (mail) => {
    const response = await axios.post(`${API_URL}/mail/sendmail`, mail);
    return response.data;
};