import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { AccountBalanceService } from './service/account-balance.service';
import { AccountBalance } from './model/account-balance';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatTableModule, CommonModule, MatCardModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'banking-account-app';

  accountList: AccountBalance[] = [];
  displayedColumns: string[] = ['account', 'username', 'balance'];
  dataSource: MatTableDataSource<AccountBalance> = new MatTableDataSource();

  constructor(private accountBalanceService: AccountBalanceService) {
    this.populateList();
  }

  populateList() {
    this.accountBalanceService.getAllAccountBalances()
    .subscribe(({ accounts }) => {
      Object.assign(this.accountList, accounts);
      this.dataSource = new MatTableDataSource<AccountBalance>(accounts)
    });
  }
}
