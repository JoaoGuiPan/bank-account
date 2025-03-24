export interface AccountBalance {
    account: string;
    balance: number;
}

export interface AccountBalanceResponse {
    accounts: AccountBalance[];
}
