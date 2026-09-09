const TRANSIENT = new Set([408, 425, 429, 500, 502, 503, 504]);

export class MetaCloudClient {
  constructor({ accessToken = process.env.META_ACCESS_TOKEN, phoneNumberId = process.env.META_PHONE_NUMBER_ID, graphVersion = process.env.META_GRAPH_API_VERSION || "v23.0", fetchImpl = fetch, maxAttempts = 3 } = {}) {
    if (!accessToken || !phoneNumberId) throw new Error("META_ACCESS_TOKEN and META_PHONE_NUMBER_ID are required");
    this.accessToken = accessToken;
    this.phoneNumberId = phoneNumberId;
    this.baseUrl = `https://graph.facebook.com/${graphVersion}`;
    this.fetchImpl = fetchImpl;
    this.maxAttempts = maxAttempts;
  }

  async send(payload) {
    let lastError;
    for (let attempt = 1; attempt <= this.maxAttempts; attempt += 1) {
      try {
        const response = await this.fetchImpl(`${this.baseUrl}/${this.phoneNumberId}/messages`, { method: "POST", headers: { authorization: `Bearer ${this.accessToken}`, "content-type": "application/json" }, body: JSON.stringify(payload) });
        const body = await response.json().catch(() => ({}));
        if (response.ok) return { externalId: body.messages?.[0]?.id || null, rawStatus: response.status };
        const error = new Error("META_GRAPH_REQUEST_FAILED"); error.status = response.status; error.code = body.error?.code || "META_ERROR";
        if (!TRANSIENT.has(response.status) || attempt === this.maxAttempts) throw error;
        lastError = error;
      } catch (error) {
        if (attempt === this.maxAttempts || !TRANSIENT.has(error.status)) throw error;
        lastError = error;
      }
      await new Promise((resolve) => setTimeout(resolve, 2 ** attempt * 100));
    }
    throw lastError || new Error("META_REQUEST_FAILED");
  }
}
